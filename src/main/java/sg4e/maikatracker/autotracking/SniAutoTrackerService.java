package sg4e.maikatracker.autotracking;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SniAutoTrackerService implements Closeable {
    private static final Logger LOG = LogManager.getLogger();
    private static final int FOUND_ADDR = 0x7E1500;
    private static final int USED_ADDR = 0x7E1503;
    private static final int CHECKED_ADDR = 0x7E1510;
    private static final int FOUND_LOCATION_ADDR = 0x707080;
    private volatile boolean enabled;
    private long lastWarningMs;

    private final SniMemoryReader reader;
    private final SniTrackerDecoder decoder;
    private final Consumer<SniTrackerSnapshot> consumer;
    private final ScheduledExecutorService executor;

    public SniAutoTrackerService(SniMemoryReader reader, SniTrackerDecoder decoder, Consumer<SniTrackerSnapshot> consumer, long pollMs) {
        this.reader = reader;
        this.decoder = decoder;
        this.consumer = consumer;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this::poll, pollMs, pollMs, TimeUnit.MILLISECONDS);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    void poll() {
        if (!enabled) return;
        try {
            SniTrackerSnapshot snapshot = decoder.decode(reader.read(FOUND_ADDR, 3), reader.read(USED_ADDR, 3), reader.read(CHECKED_ADDR, 16), reader.read(FOUND_LOCATION_ADDR, 34));
            consumer.accept(snapshot);
        } catch (Exception ex) {
            long now = System.currentTimeMillis();
            if (now - lastWarningMs > 5000) {
                LOG.warn("SNI auto-tracking poll failed: {}", ex.getMessage());
                lastWarningMs = now;
            }
        }
    }

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
    }
}
