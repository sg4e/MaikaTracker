package sg4e.maikatracker.integration;

import org.junit.Assert;
import org.junit.Test;
import sg4e.maikatracker.KeyItemMetadata;
import sg4e.ff4stats.fe.KeyItemLocation;

public class FeTrackerMappingsTest {
    @Test
    public void hasExpectedMappings() {
        Assert.assertEquals(KeyItemMetadata.CRYSTAL, FeTrackerMappings.keyItem(0x00));
        Assert.assertEquals(KeyItemMetadata.PINK_TAIL, FeTrackerMappings.keyItem(0x11));
        Assert.assertEquals(KeyItemLocation.START, FeTrackerMappings.location(0x20));
        Assert.assertEquals(KeyItemLocation.ZEROMUS, FeTrackerMappings.location(0x3D));
    }
}
