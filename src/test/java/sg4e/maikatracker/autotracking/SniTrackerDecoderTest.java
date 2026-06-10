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
        checked[4] = (byte) 0b00000011;
        checked[15] = (byte) 0b10000000; // unmapped ignored

        SniTrackerSnapshot snap = decoder.decode(found, used, checked);
        Assert.assertTrue(snap.getFound().contains(KeyItemMetadata.PACKAGE));
        Assert.assertTrue(snap.getFound().contains(KeyItemMetadata.SAND_RUBY));
        Assert.assertTrue(snap.getUsed().contains(KeyItemMetadata.SAND_RUBY));
        Assert.assertEquals(2, snap.getCheckedLocations().size());
    }

    @Test
    public void mappingHasNoDuplicateKeyIndices() {
        Assert.assertEquals(SniTrackerMappings.KEY_ITEM_BY_BIT.size(), SniTrackerMappings.KEY_ITEM_BY_BIT.keySet().stream().distinct().count());
        Assert.assertNotNull(SniTrackerMappings.KEY_ITEM_BY_BIT.get(0));
    }

    @Test
    public void passIsNotAutotrackable() {
        Assert.assertFalse(SniTrackerMappings.AUTOTRACKABLE_KEY_ITEMS.contains(KeyItemMetadata.PASS));
    }

    @Test
    public void allMappedItemsAreAutotrackable() {
        SniTrackerMappings.KEY_ITEM_BY_BIT.values().forEach(item ->
            Assert.assertTrue(item + " should be autotrackable", SniTrackerMappings.AUTOTRACKABLE_KEY_ITEMS.contains(item))
        );
    }

    @Test
    public void autotrackableItemCountMatchesMappedItems() {
        Assert.assertEquals(SniTrackerMappings.KEY_ITEM_BY_BIT.values().stream().distinct().count(),
                SniTrackerMappings.AUTOTRACKABLE_KEY_ITEMS.size());
    }

    @Test
    public void decodedSnapshotNeverContainsPass() {
        SniTrackerDecoder decoder = new SniTrackerDecoder();
        // All 24 bits set in each field
        byte[] allBitsSet = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] checked = new byte[16];
        java.util.Arrays.fill(checked, (byte) 0xFF);

        SniTrackerSnapshot snap = decoder.decode(allBitsSet, allBitsSet, checked);
        Assert.assertFalse("Pass must never appear in decoded found set", snap.getFound().contains(KeyItemMetadata.PASS));
        Assert.assertFalse("Pass must never appear in decoded used set", snap.getUsed().contains(KeyItemMetadata.PASS));
    }
}
