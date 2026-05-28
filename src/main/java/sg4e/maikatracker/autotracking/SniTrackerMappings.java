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
        keyItems.put(2, KeyItemMetadata.BARON_KEY);
        keyItems.put(3, KeyItemMetadata.TWIN_HARP);
        keyItems.put(4, KeyItemMetadata.EARTH);
        keyItems.put(5, KeyItemMetadata.MAGMA_KEY);
        keyItems.put(6, KeyItemMetadata.TOWER_KEY);
        keyItems.put(7, KeyItemMetadata.HOOK);
        keyItems.put(8, KeyItemMetadata.LUCA_KEY);
        keyItems.put(9, KeyItemMetadata.DARKNESS);
        keyItems.put(10, KeyItemMetadata.RAT_TAIL);
        keyItems.put(11, KeyItemMetadata.PAN);
        keyItems.put(12, KeyItemMetadata.ADAMANT);
        keyItems.put(13, KeyItemMetadata.LEGEND);
        keyItems.put(14, KeyItemMetadata.SPOON);
        keyItems.put(15, KeyItemMetadata.PINK_TAIL);
        keyItems.put(16, KeyItemMetadata.PASS);
        keyItems.put(17, KeyItemMetadata.CRYSTAL);
        KEY_ITEM_BY_BIT = Collections.unmodifiableMap(keyItems);

        Map<Integer, KeyItemLocation> locations = new HashMap<>();
        locations.put(0, KeyItemLocation.START);
        locations.put(1, KeyItemLocation.ANTLION);
        locations.put(2, KeyItemLocation.FABUL);
        locations.put(3, KeyItemLocation.ORDEALS);
        locations.put(4, KeyItemLocation.BARON_INN);
        locations.put(5, KeyItemLocation.BARON_CASTLE);
        locations.put(6, KeyItemLocation.ZOT);
        locations.put(7, KeyItemLocation.DWARF_CASTLE);
        locations.put(8, KeyItemLocation.LOW_BABIL);
        locations.put(9, KeyItemLocation.TOP_BABIL);
        locations.put(10, KeyItemLocation.SEALED_CAVE);
        locations.put(11, KeyItemLocation.SYLPH);
        locations.put(12, KeyItemLocation.ASURA);
        locations.put(13, KeyItemLocation.LEVIATAN);
        locations.put(14, KeyItemLocation.ODIN);
        locations.put(15, KeyItemLocation.BAHAMUT);
        locations.put(16, KeyItemLocation.DLUNAR);
        locations.put(17, KeyItemLocation.PALE_DIM);
        locations.put(18, KeyItemLocation.PLAGUE);
        locations.put(19, KeyItemLocation.OGOPOGO);
        locations.put(20, KeyItemLocation.WYVERN);
        LOCATION_BY_BIT = Collections.unmodifiableMap(locations);
    }
}
