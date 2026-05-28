package sg4e.maikatracker.autotracking;

import com.github.alttpo.sni.AddressSpace;
import com.github.alttpo.sni.DeviceCapability;
import com.github.alttpo.sni.DeviceMemoryGrpc;
import com.github.alttpo.sni.DetectMemoryMappingRequest;
import com.github.alttpo.sni.DevicesGrpc;
import com.github.alttpo.sni.DevicesRequest;
import com.github.alttpo.sni.DevicesResponse;
import com.github.alttpo.sni.MemoryMapping;
import com.github.alttpo.sni.ReadMemoryRequest;
import com.github.alttpo.sni.SingleReadMemoryRequest;
import com.github.alttpo.sni.SingleReadMemoryResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SniGrpcMemoryReader implements SniMemoryReader {
    private static final long SHUTDOWN_TIMEOUT_MS = 1000L;

    private enum AddressMode { AUTO, SNES_ABUS, RAW, FXPAKPRO }

    private final ManagedChannel channel;
    private final DevicesGrpc.DevicesBlockingStub devicesStub;
    private final DeviceMemoryGrpc.DeviceMemoryBlockingStub memoryStub;
    private final String preferredDeviceUri;
    private final AddressMode addressMode;

    private volatile DevicesResponse.Device cachedDevice;
    private volatile MemoryMapping cachedMemoryMapping;

    public SniGrpcMemoryReader(String grpcTarget) {
        this(grpcTarget, System.getenv("MAIKA_SNI_DEVICE_URI"));
    }

    public SniGrpcMemoryReader(String grpcTarget, String preferredDeviceUri) {
        this.channel = ManagedChannelBuilder.forTarget(grpcTarget).usePlaintext().build();
        this.devicesStub = DevicesGrpc.newBlockingStub(channel);
        this.memoryStub = DeviceMemoryGrpc.newBlockingStub(channel);
        this.preferredDeviceUri = preferredDeviceUri;
        this.addressMode = parseMode(System.getenv("MAIKA_SNI_ADDRESS_MODE"));
    }

    @Override
    public synchronized byte[] read(int snesAddress, int length) throws IOException {
        DevicesResponse.Device device = resolveDevice();
        ReadMemoryRequest request = buildRequest(device, snesAddress, length);
        SingleReadMemoryResponse response = memoryStub.singleRead(SingleReadMemoryRequest.newBuilder()
                .setUri(device.getUri())
                .setRequest(request)
                .build());
        byte[] data = response.getResponse().getData().toByteArray();
        if (data.length != length) {
            throw new IOException("Expected " + length + " bytes but received " + data.length);
        }
        return data;
    }

    private ReadMemoryRequest buildRequest(DevicesResponse.Device device, int snesAddress, int length) throws IOException {
        AddressMode mode = resolveMode(device);
        ReadMemoryRequest.Builder builder = ReadMemoryRequest.newBuilder().setSize(length);

        switch (mode) {
            case RAW:
                builder.setRequestAddressSpace(AddressSpace.Raw).setRequestAddress(snesAddress);
                break;
            case FXPAKPRO:
                builder.setRequestAddressSpace(AddressSpace.FxPakPro).setRequestAddress(toFxPakProAddress(snesAddress));
                break;
            case SNES_ABUS:
            default:
                builder.setRequestAddressSpace(AddressSpace.SnesABus)
                       .setRequestAddress(snesAddress)
                       .setRequestMemoryMapping(resolveMemoryMapping(device.getUri()));
                break;
        }

        return builder.build();
    }

    private AddressMode resolveMode(DevicesResponse.Device device) {
        if (addressMode != AddressMode.AUTO) return addressMode;
        if ("retroarch".equals(device.getKind())) return AddressMode.RAW;
        return AddressMode.SNES_ABUS;
    }

    private int toFxPakProAddress(int snesAddress) throws IOException {
        if (snesAddress < 0x7E0000 || snesAddress > 0x7FFFFF) {
            throw new IOException("FXPakPro mode currently supports WRAM addresses only: " + Integer.toHexString(snesAddress));
        }
        return 0xF50000 + (snesAddress - 0x7E0000);
    }

    private MemoryMapping resolveMemoryMapping(String uri) throws IOException {
        if (cachedDevice != null && uri.equals(cachedDevice.getUri()) && cachedMemoryMapping != null && cachedMemoryMapping != MemoryMapping.Unknown) {
            return cachedMemoryMapping;
        }
        MemoryMapping mapping = memoryStub.mappingDetect(DetectMemoryMappingRequest.newBuilder().setUri(uri).build()).getMemoryMapping();
        if (mapping == null || mapping == MemoryMapping.Unknown) {
            throw new IOException("SNI MappingDetect returned Unknown mapping for device: " + uri);
        }
        cachedMemoryMapping = mapping;
        return mapping;
    }

    private DevicesResponse.Device resolveDevice() throws IOException {
        if (cachedDevice != null) return cachedDevice;
        List<DevicesResponse.Device> devices = devicesStub.listDevices(DevicesRequest.newBuilder().build()).getDevicesList();
        if (devices.isEmpty()) throw new IOException("No SNI devices available");

        if (preferredDeviceUri != null && !preferredDeviceUri.trim().isEmpty()) {
            for (DevicesResponse.Device device : devices) {
                if (preferredDeviceUri.equals(device.getUri())) {
                    cachedDevice = device;
                    return cachedDevice;
                }
            }
            throw new IOException("Configured SNI device URI not found: " + preferredDeviceUri);
        }

        for (DevicesResponse.Device device : devices) {
            if (device.getCapabilitiesList().contains(DeviceCapability.ReadMemory)) {
                cachedDevice = device;
                return cachedDevice;
            }
        }

        cachedDevice = devices.get(0);
        return cachedDevice;
    }

    private AddressMode parseMode(String value) {
        if (value == null || value.trim().isEmpty()) return AddressMode.AUTO;
        try {
            return AddressMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return AddressMode.AUTO;
        }
    }

    public void close() {
        channel.shutdown();
        try {
            if (!channel.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                channel.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            channel.shutdownNow();
        }
    }
}
