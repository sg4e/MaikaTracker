package sg4e.maikatracker.integration;

import org.junit.Assert;
import org.junit.Test;
import sg4e.maikatracker.KeyItemMetadata;
import sg4e.ff4stats.fe.KeyItemLocation;

public class FeMemoryDecoderTest {
    @Test
    public void decodesKeyItemsAndLocations() {
        byte[] found = new byte[] {0x03, 0x00, 0x00}; // crystal, pass
        byte[] used = new byte[] {0x01, 0x00, 0x00}; // crystal
        byte[] checked = new byte[16];
        checked[0x20 / 8] |= (1 << (0x20 % 8)); // START
        checked[0x2A / 8] |= (1 << (0x2A % 8)); // DWARF_CASTLE

        FeTrackerSnapshot snapshot = FeMemoryDecoder.decode(found, used, checked);

        Assert.assertTrue(snapshot.getFound().contains(KeyItemMetadata.CRYSTAL));
        Assert.assertTrue(snapshot.getFound().contains(KeyItemMetadata.PASS));
        Assert.assertTrue(snapshot.getUsed().contains(KeyItemMetadata.CRYSTAL));
        Assert.assertTrue(snapshot.getCheckedLocations().contains(KeyItemLocation.START));
        Assert.assertTrue(snapshot.getCheckedLocations().contains(KeyItemLocation.DWARF_CASTLE));
    }
}
