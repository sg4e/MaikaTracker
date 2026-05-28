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
        AddressMode mode = resolveMode(device);

        byte[] primary = singleRead(device.getUri(), buildRequest(device, snesAddress, length, mode), length);
        if (mode == AddressMode.FXPAKPRO && isAllZero(primary)) {
            byte[] fallback = singleRead(device.getUri(), buildRequest(device, snesAddress, length, AddressMode.SNES_ABUS), length);
            if (!isAllZero(fallback)) {
                return fallback;
            }
        }
        return primary;
    }

    private ReadMemoryRequest buildRequest(DevicesResponse.Device device, int snesAddress, int length, AddressMode mode) throws IOException {
        ReadMemoryRequest.Builder builder = ReadMemoryRequest.newBuilder().setSize(length);

        switch (mode) {
            case RAW:
                builder.setRequestAddressSpace(AddressSpace.Raw).setRequestAddress(snesAddress);
                break;
            case FXPAKPRO:
                Integer fxAddress = toFxPakProAddressOrNull(snesAddress);
                if (fxAddress != null) {
                    builder.setRequestAddressSpace(AddressSpace.FxPakPro).setRequestAddress(fxAddress);
                } else {
                    builder.setRequestAddressSpace(AddressSpace.SnesABus)
                           .setRequestAddress(snesAddress)
                           .setRequestMemoryMapping(resolveMemoryMapping(device.getUri()));
                }
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


    private byte[] singleRead(String uri, ReadMemoryRequest request, int length) throws IOException {
        SingleReadMemoryResponse response = memoryStub.singleRead(SingleReadMemoryRequest.newBuilder()
                .setUri(uri)
                .setRequest(request)
                .build());
        byte[] data = response.getResponse().getData().toByteArray();
        if (data.length != length) {
            throw new IOException("Expected " + length + " bytes but received " + data.length);
        }
        return data;
    }

    private boolean isAllZero(byte[] data) {
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }
    private AddressMode resolveMode(DevicesResponse.Device device) {
        if (addressMode != AddressMode.AUTO) return addressMode;
        if ("retroarch".equals(device.getKind())) return AddressMode.RAW;
        if ("fxpakpro".equals(device.getKind())) return AddressMode.FXPAKPRO;
        return AddressMode.SNES_ABUS;
    }

    private Integer toFxPakProAddressOrNull(int snesAddress) {
        if (snesAddress >= 0x7E0000 && snesAddress <= 0x7FFFFF) {
            return 0xF50000 + (snesAddress - 0x7E0000);
        }
        return null;
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
