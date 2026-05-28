package sg4e.maikatracker.autotracking;

import org.junit.Assert;
import org.junit.Test;
import sg4e.maikatracker.KeyItemMetadata;

public class SniTrackerDecoderTest {
    @Test
    public void decodeFoundUsedCheckedAndIgnoreUnknown() {
        SniTrackerDecoder decoder = new SniTrackerDecoder();
        byte[] found = new byte[] {0b00000011, 0, 0};
        byte[] used = new byte[] {0b00000010, 0, 0};
        byte[] checked = new byte[16];
        checked[0] = (byte) 0b00000011;
        checked[15] = (byte) 0b10000000; // unmapped ignored

        SniTrackerSnapshot snap = decoder.decode(found, used, checked);
        Assert.assertTrue(snap.getFound().contains(KeyItemMetadata.CRYSTAL));
        Assert.assertTrue(snap.getFound().contains(KeyItemMetadata.PASS));
        Assert.assertTrue(snap.getUsed().contains(KeyItemMetadata.PASS));
        Assert.assertEquals(2, snap.getCheckedLocations().size());
    }

    @Test
    public void mappingHasNoDuplicateKeyIndices() {
        Assert.assertEquals(SniTrackerMappings.KEY_ITEM_BY_BIT.size(), SniTrackerMappings.KEY_ITEM_BY_BIT.keySet().stream().distinct().count());
        Assert.assertNotNull(SniTrackerMappings.KEY_ITEM_BY_BIT.get(0));
    }
}
