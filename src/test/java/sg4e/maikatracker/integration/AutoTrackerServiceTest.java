package sg4e.maikatracker.integration;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;

public class AutoTrackerServiceTest {
    @Test
    public void doesNotPollWhenDisabled() {
        AtomicBoolean called = new AtomicBoolean(false);
        MemoryReader reader = (address, length) -> { called.set(true); return new byte[length]; };
        AutoTrackerService service = new AutoTrackerService(reader, snapshot -> {});

        boolean polled = service.pollOnce();

        Assert.assertFalse(polled);
        Assert.assertFalse(called.get());
    }

    @Test
    public void pollsWhenEnabled() {
        AtomicBoolean consumed = new AtomicBoolean(false);
        MemoryReader reader = (address, length) -> new byte[length];
        AutoTrackerService service = new AutoTrackerService(reader, snapshot -> consumed.set(true));
        service.setEnabled(true);

        boolean polled = service.pollOnce();

        Assert.assertTrue(polled);
        Assert.assertTrue(consumed.get());
    }
}
