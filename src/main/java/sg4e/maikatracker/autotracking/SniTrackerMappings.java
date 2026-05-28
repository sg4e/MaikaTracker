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
        KeyItemMetadata[] values = KeyItemMetadata.values();
        for (int i = 0; i < values.length; i++) keyItems.put(i, values[i]);
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
