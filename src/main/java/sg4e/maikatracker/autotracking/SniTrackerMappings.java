package sg4e.maikatracker.autotracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.maikatracker.KeyItemMetadata;

public final class SniTrackerMappings {
    private SniTrackerMappings() {}
    public static final Map<Integer, KeyItemMetadata> KEY_ITEM_BY_BIT;
    public static final Map<Integer, KeyItemLocation> LOCATION_BY_BIT;
    static {
        Map<Integer, KeyItemMetadata> keyItems = new HashMap<>();
        // FE SNI bitfield order for found/used key items (bit 0 is Package, not Crystal).
        keyItems.put(0, KeyItemMetadata.PACKAGE);
        keyItems.put(1, KeyItemMetadata.SAND_RUBY);
        keyItems.put(2, KeyItemMetadata.LEGEND);
        keyItems.put(3, KeyItemMetadata.BARON_KEY);
        keyItems.put(4, KeyItemMetadata.TWIN_HARP);
        keyItems.put(5, KeyItemMetadata.EARTH);
        keyItems.put(6, KeyItemMetadata.MAGMA_KEY);
        keyItems.put(7, KeyItemMetadata.TOWER_KEY);
        keyItems.put(8, KeyItemMetadata.HOOK);
        keyItems.put(9, KeyItemMetadata.LUCA_KEY);
        keyItems.put(10, KeyItemMetadata.DARKNESS);
        keyItems.put(11, KeyItemMetadata.RAT_TAIL);
        keyItems.put(12, KeyItemMetadata.ADAMANT);
        keyItems.put(13, KeyItemMetadata.PAN);
        keyItems.put(14, KeyItemMetadata.SPOON);
        keyItems.put(15, KeyItemMetadata.PINK_TAIL);
        keyItems.put(16, KeyItemMetadata.CRYSTAL);
        KEY_ITEM_BY_BIT = Collections.unmodifiableMap(keyItems);

        Map<Integer, KeyItemLocation> locations = new HashMap<>();
        // FE location bits are indexed from 0x20, not 0x00.
        locations.put(0x20, KeyItemLocation.START);
        locations.put(0x21, KeyItemLocation.ANTLION);
        locations.put(0x22, KeyItemLocation.FABUL);
        locations.put(0x23, KeyItemLocation.ORDEALS);
        locations.put(0x24, KeyItemLocation.BARON_INN);
        locations.put(0x25, KeyItemLocation.BARON_CASTLE);
        locations.put(0x26, KeyItemLocation.TOROIA);
        locations.put(0x27, KeyItemLocation.DARK_ELF);
        locations.put(0x28, KeyItemLocation.ZOT);
        locations.put(0x29, KeyItemLocation.TOP_BABIL);
        locations.put(0x2A, KeyItemLocation.LOW_BABIL);
        locations.put(0x2B, KeyItemLocation.DWARF_CASTLE);
        locations.put(0x2C, KeyItemLocation.SEALED_CAVE);
        locations.put(0x2D, KeyItemLocation.SUMMONED_MONSTERS_CHEST);
        locations.put(0x2E, KeyItemLocation.RAT_TAIL);
        locations.put(0x2F, KeyItemLocation.SHEILA_PANLESS);
        locations.put(0x30, KeyItemLocation.SHEILA_PAN);
        locations.put(0x31, KeyItemLocation.ASURA);
        locations.put(0x32, KeyItemLocation.LEVIATAN);
        locations.put(0x33, KeyItemLocation.ODIN);
        locations.put(0x34, KeyItemLocation.SYLPH);
        locations.put(0x35, KeyItemLocation.BAHAMUT);
        locations.put(0x36, KeyItemLocation.PALE_DIM);
        locations.put(0x37, KeyItemLocation.WYVERN);
        locations.put(0x38, KeyItemLocation.PLAGUE);
        locations.put(0x39, KeyItemLocation.DLUNAR);
        locations.put(0x3A, KeyItemLocation.DLUNAR);
        locations.put(0x3B, KeyItemLocation.OGOPOGO);
        locations.put(0x59, KeyItemLocation.MIST);
        locations.put(0x5A, KeyItemLocation.ZEROMUS);
        locations.put(0x5D, KeyItemLocation.OBJECTIVE);
        LOCATION_BY_BIT = Collections.unmodifiableMap(locations);
    }
}
