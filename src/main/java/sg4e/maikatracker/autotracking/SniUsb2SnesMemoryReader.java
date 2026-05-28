package sg4e.maikatracker.autotracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class SniUsb2SnesMemoryReader implements SniMemoryReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long TIMEOUT_MS = 3000L;

    private final URI endpoint;
    private final LinkedBlockingQueue<String> textResponses = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<byte[]> binaryResponses = new LinkedBlockingQueue<>();
    private Usb2SnesClient client;
    private volatile boolean attached;

    public SniUsb2SnesMemoryReader(String endpointUrl) {
        this.endpoint = URI.create(endpointUrl);
    }

    @Override
    public synchronized byte[] read(int snesAddress, int length) throws IOException {
        ensureConnectedAndAttached();
        sendCommand("GetAddress", "SNES", hexAddress(snesAddress), hexAddress(length));
        byte[] data = pollBinary();
        if (data.length != length) {
            throw new IOException("Expected " + length + " bytes but received " + data.length);
        }
        return data;
    }

    private void ensureConnectedAndAttached() throws IOException {
        if (client == null || !client.isOpen()) {
            attached = false;
            textResponses.clear();
            binaryResponses.clear();
            client = new Usb2SnesClient(endpoint);
            try {
                if (!client.connectBlocking(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    throw new IOException("Unable to connect to SNI at " + endpoint);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while connecting to SNI", ex);
            }
            sendCommand("Name", "SNES", "MaikaTracker");
        }
        if (!attached) {
            sendCommand("DeviceList", "SNES");
            JsonNode deviceList = pollJson();
            List<String> devices = new ArrayList<>();
            JsonNode results = deviceList.get("Results");
            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    devices.add(node.asText());
                }
            }
            if (devices.isEmpty()) {
                throw new IOException("No SNI devices available");
            }
            sendCommand("Attach", "SNES", devices.get(0));
            attached = true;
        }
    }

    private void sendCommand(String opcode, String space, String... operands) throws IOException {
        String payload = buildPayload(opcode, space, operands);
        if (!client.sendPayload(payload)) {
            throw new IOException("Failed to send " + opcode + " command to SNI");
        }
    }

    private String buildPayload(String opcode, String space, String... operands) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"Opcode\":\"").append(opcode).append("\",\"Space\":\"").append(space).append("\"");
        if (operands.length > 0) {
            builder.append(",\"Operands\":[");
            for (int i = 0; i < operands.length; i++) {
                if (i > 0) builder.append(',');
                builder.append('"').append(operands[i]).append('"');
            }
            builder.append(']');
        }
        builder.append('}');
        return builder.toString();
    }

    private JsonNode pollJson() throws IOException {
        try {
            String text = textResponses.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (text == null) throw new IOException("Timed out waiting for SNI response");
            return MAPPER.readTree(text);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for SNI response", ex);
        }
    }

    private byte[] pollBinary() throws IOException {
        try {
            byte[] data = binaryResponses.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (data == null) throw new IOException("Timed out waiting for SNI memory data");
            return data;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for SNI memory data", ex);
        }
    }

    private String hexAddress(int value) {
        return String.format(Locale.ROOT, "%06X", value & 0xFFFFFF);
    }

    private final class Usb2SnesClient extends WebSocketClient {
        Usb2SnesClient(URI serverUri) {
            super(serverUri);
        }

        boolean sendPayload(String payload) {
            if (!isOpen()) return false;
            send(payload);
            return true;
        }

        @Override public void onOpen(ServerHandshake handshakedata) {}

        @Override public void onMessage(String message) {
            textResponses.offer(message);
        }

        @Override public void onMessage(ByteBuffer bytes) {
            byte[] data = new byte[bytes.remaining()];
            bytes.get(data);
            binaryResponses.offer(data);
        }

        @Override public void onClose(int code, String reason, boolean remote) {
            attached = false;
        }

        @Override public void onError(Exception ex) {
            attached = false;
        }
    }
}
