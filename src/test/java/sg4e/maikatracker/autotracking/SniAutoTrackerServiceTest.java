package sg4e.maikatracker.autotracking;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class SniAutoTrackerServiceTest {
    @Test
    public void disabledServiceDoesNotEmit() {
        AtomicInteger emits = new AtomicInteger();
        SniAutoTrackerService svc = new SniAutoTrackerService((a, l) -> new byte[l], new SniTrackerDecoder(), s -> emits.incrementAndGet(), 10000);
        svc.poll();
        Assert.assertEquals(0, emits.get());
    }

    @Test
    public void enabledServiceReadsAndEmits() {
        AtomicInteger emits = new AtomicInteger();
        SniAutoTrackerService svc = new SniAutoTrackerService((a, l) -> new byte[l], new SniTrackerDecoder(), s -> emits.incrementAndGet(), 10000);
        svc.setEnabled(true);
        svc.poll();
        Assert.assertEquals(1, emits.get());
    }

    @Test
    public void readFailureDoesNotEmit() {
        AtomicInteger emits = new AtomicInteger();
        SniAutoTrackerService svc = new SniAutoTrackerService((a, l) -> { throw new IOException("boom"); }, new SniTrackerDecoder(), s -> emits.incrementAndGet(), 10000);
        svc.setEnabled(true);
        svc.poll();
        Assert.assertEquals(0, emits.get());
    }
}
