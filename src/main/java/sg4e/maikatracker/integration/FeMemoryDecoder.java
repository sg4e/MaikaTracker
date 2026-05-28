package sg4e.maikatracker.integration;

import java.util.HashSet;
import java.util.Set;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.maikatracker.KeyItemMetadata;

public final class FeMemoryDecoder {
    private FeMemoryDecoder() {}

    public static FeTrackerSnapshot decode(byte[] found, byte[] used, byte[] checkedLocations) {
        if (found.length != 3 || used.length != 3 || checkedLocations.length != 16) {
            throw new IllegalArgumentException("Unexpected tracker byte lengths");
        }

        Set<KeyItemMetadata> foundSet = decodeKeyItems(found);
        Set<KeyItemMetadata> usedSet = decodeKeyItems(used);
        Set<KeyItemLocation> locationsSet = decodeLocations(checkedLocations);
        return new FeTrackerSnapshot(foundSet, usedSet, locationsSet);
    }

    static Set<KeyItemMetadata> decodeKeyItems(byte[] bytes) {
        Set<KeyItemMetadata> set = new HashSet<>();
        for (int i = 0; i < 0x18; i++) {
            if ((bytes[i / 8] & (1 << (i % 8))) != 0) {
                KeyItemMetadata mapped = FeTrackerMappings.keyItem(i);
                if (mapped != null) {
                    set.add(mapped);
                }
            }
        }
        return set;
    }

    static Set<KeyItemLocation> decodeLocations(byte[] bytes) {
        Set<KeyItemLocation> set = new HashSet<>();
        for (int i = 0; i < 0x80; i++) {
            if ((bytes[i / 8] & (1 << (i % 8))) != 0) {
                KeyItemLocation mapped = FeTrackerMappings.location(i);
                if (mapped != null) {
                    set.add(mapped);
                }
            }
        }
        return set;
    }
}
