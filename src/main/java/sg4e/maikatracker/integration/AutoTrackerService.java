package sg4e.maikatracker.integration;

import java.util.function.Consumer;

public class AutoTrackerService {
    static final int FOUND_ADDR = 0x7E1500;
    static final int USED_ADDR = 0x7E1503;
    static final int CHECKED_LOC_ADDR = 0x7E1510;

    private final MemoryReader reader;
    private final Consumer<FeTrackerSnapshot> consumer;
    private boolean enabled;

    public AutoTrackerService(MemoryReader reader, Consumer<FeTrackerSnapshot> consumer) {
        this.reader = reader;
        this.consumer = consumer;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean pollOnce() {
        if (!enabled) {
            return false;
        }
        byte[] found = reader.read(FOUND_ADDR, 3);
        byte[] used = reader.read(USED_ADDR, 3);
        byte[] locations = reader.read(CHECKED_LOC_ADDR, 16);
        FeTrackerSnapshot snapshot = FeMemoryDecoder.decode(found, used, locations);
        consumer.accept(snapshot);
        return true;
    }
}
