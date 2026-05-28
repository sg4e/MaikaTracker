package sg4e.maikatracker.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.maikatracker.KeyItemMetadata;

public final class FeTrackerMappings {
    private static final Map<Integer, KeyItemMetadata> KEY_ITEMS;
    private static final Map<Integer, KeyItemLocation> LOCATIONS;

    static {
        Map<Integer, KeyItemMetadata> keyItems = new HashMap<>();
        keyItems.put(0x00, KeyItemMetadata.CRYSTAL);
        keyItems.put(0x01, KeyItemMetadata.PASS);
        keyItems.put(0x02, KeyItemMetadata.HOOK);
        keyItems.put(0x03, KeyItemMetadata.DARKNESS);
        keyItems.put(0x04, KeyItemMetadata.EARTH);
        keyItems.put(0x05, KeyItemMetadata.TWIN_HARP);
        keyItems.put(0x06, KeyItemMetadata.PACKAGE);
        keyItems.put(0x07, KeyItemMetadata.SAND_RUBY);
        keyItems.put(0x08, KeyItemMetadata.BARON_KEY);
        keyItems.put(0x09, KeyItemMetadata.MAGMA_KEY);
        keyItems.put(0x0A, KeyItemMetadata.TOWER_KEY);
        keyItems.put(0x0B, KeyItemMetadata.LUCA_KEY);
        keyItems.put(0x0C, KeyItemMetadata.ADAMANT);
        keyItems.put(0x0D, KeyItemMetadata.LEGEND);
        keyItems.put(0x0E, KeyItemMetadata.PAN);
        keyItems.put(0x0F, KeyItemMetadata.SPOON);
        keyItems.put(0x10, KeyItemMetadata.RAT_TAIL);
        keyItems.put(0x11, KeyItemMetadata.PINK_TAIL);
        KEY_ITEMS = Collections.unmodifiableMap(keyItems);

        Map<Integer, KeyItemLocation> locations = new HashMap<>();
        locations.put(0x20, KeyItemLocation.START);
        locations.put(0x21, KeyItemLocation.ANTLION);
        locations.put(0x22, KeyItemLocation.FABUL);
        locations.put(0x23, KeyItemLocation.ORDEALS);
        locations.put(0x24, KeyItemLocation.BARON_INN);
        locations.put(0x25, KeyItemLocation.BARON_CASTLE);
        locations.put(0x26, KeyItemLocation.DARK_ELF);
        locations.put(0x27, KeyItemLocation.ZOT);
        locations.put(0x28, KeyItemLocation.TOP_BABIL);
        locations.put(0x29, KeyItemLocation.LOW_BABIL);
        locations.put(0x2A, KeyItemLocation.DWARF_CASTLE);
        locations.put(0x2B, KeyItemLocation.SEALED_CAVE);
        locations.put(0x2C, KeyItemLocation.SUMMONED_MONSTERS_CHEST);
        locations.put(0x2D, KeyItemLocation.RAT_TAIL);
        locations.put(0x2E, KeyItemLocation.SHEILA_PANLESS);
        locations.put(0x2F, KeyItemLocation.SHEILA_PAN);
        locations.put(0x30, KeyItemLocation.ASURA);
        locations.put(0x31, KeyItemLocation.LEVIATAN);
        locations.put(0x32, KeyItemLocation.ODIN);
        locations.put(0x33, KeyItemLocation.SYLPH);
        locations.put(0x34, KeyItemLocation.BAHAMUT);
        locations.put(0x35, KeyItemLocation.PALE_DIM);
        locations.put(0x36, KeyItemLocation.WYVERN);
        locations.put(0x37, KeyItemLocation.PLAGUE);
        locations.put(0x38, KeyItemLocation.DLUNAR);
        locations.put(0x39, KeyItemLocation.OGOPOGO);
        locations.put(0x3A, KeyItemLocation.MIST);
        locations.put(0x3B, KeyItemLocation.KOKKOL);
        locations.put(0x3C, KeyItemLocation.OBJECTIVE);
        locations.put(0x3D, KeyItemLocation.ZEROMUS);
        LOCATIONS = Collections.unmodifiableMap(locations);
    }

    private FeTrackerMappings() {}

    public static KeyItemMetadata keyItem(int index) { return KEY_ITEMS.get(index); }
    public static KeyItemLocation location(int index) { return LOCATIONS.get(index); }
}
