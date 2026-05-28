package sg4e.maikatracker.autotracking;

import com.github.alttpo.sni.AddressSpace;
import com.github.alttpo.sni.DeviceCapability;
import com.github.alttpo.sni.DeviceMemoryGrpc;
import com.github.alttpo.sni.DevicesGrpc;
import com.github.alttpo.sni.DevicesRequest;
import com.github.alttpo.sni.DevicesResponse;
import com.github.alttpo.sni.ReadMemoryRequest;
import com.github.alttpo.sni.SingleReadMemoryRequest;
import com.github.alttpo.sni.SingleReadMemoryResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SniGrpcMemoryReader implements SniMemoryReader {
    private static final long SHUTDOWN_TIMEOUT_MS = 1000L;

    private final ManagedChannel channel;
    private final DevicesGrpc.DevicesBlockingStub devicesStub;
    private final DeviceMemoryGrpc.DeviceMemoryBlockingStub memoryStub;
    private final String preferredDeviceUri;

    public SniGrpcMemoryReader(String grpcTarget) {
        this(grpcTarget, System.getenv("MAIKA_SNI_DEVICE_URI"));
    }

    public SniGrpcMemoryReader(String grpcTarget, String preferredDeviceUri) {
        this.channel = ManagedChannelBuilder.forTarget(grpcTarget).usePlaintext().build();
        this.devicesStub = DevicesGrpc.newBlockingStub(channel);
        this.memoryStub = DeviceMemoryGrpc.newBlockingStub(channel);
        this.preferredDeviceUri = preferredDeviceUri;
    }

    @Override
    public byte[] read(int snesAddress, int length) throws IOException {
        String uri = resolveDeviceUri();
        SingleReadMemoryResponse response = memoryStub.singleRead(SingleReadMemoryRequest.newBuilder()
                .setUri(uri)
                .setRequest(ReadMemoryRequest.newBuilder()
                        .setRequestAddress(snesAddress)
                        .setRequestAddressSpace(AddressSpace.SnesABus)
                        .setSize(length)
                        .build())
                .build());
        byte[] data = response.getResponse().getData().toByteArray();
        if (data.length != length) {
            throw new IOException("Expected " + length + " bytes but received " + data.length);
        }
        return data;
    }

    private String resolveDeviceUri() throws IOException {
        List<DevicesResponse.Device> devices = devicesStub.listDevices(DevicesRequest.newBuilder().build()).getDevicesList();
        if (devices.isEmpty()) {
            throw new IOException("No SNI devices available");
        }

        if (preferredDeviceUri != null && !preferredDeviceUri.trim().isEmpty()) {
            for (DevicesResponse.Device device : devices) {
                if (preferredDeviceUri.equals(device.getUri())) {
                    return device.getUri();
                }
            }
            throw new IOException("Configured SNI device URI not found: " + preferredDeviceUri);
        }

        for (DevicesResponse.Device device : devices) {
            if (device.getCapabilitiesList().contains(DeviceCapability.ReadMemory)) {
                return device.getUri();
            }
        }

        return devices.get(0).getUri();
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
