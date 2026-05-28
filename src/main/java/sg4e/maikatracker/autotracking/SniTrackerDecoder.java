package sg4e.maikatracker.autotracking;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.maikatracker.KeyItemMetadata;

public final class SniTrackerDecoder {
    public SniTrackerSnapshot decode(byte[] found, byte[] used, byte[] checked) {
        return new SniTrackerSnapshot(
            decodeBits(found, SniTrackerMappings.KEY_ITEM_BY_BIT),
            decodeBits(used, SniTrackerMappings.KEY_ITEM_BY_BIT),
            decodeBits(checked, SniTrackerMappings.LOCATION_BY_BIT)
        );
    }

    private <T> Set<T> decodeBits(byte[] bytes, Map<Integer, T> mapping) {
        Set<T> result = new HashSet<>();
        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
            int value = bytes[byteIndex] & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                if ((value & (1 << bit)) != 0) {
                    T mapped = mapping.get(byteIndex * 8 + bit);
                    if (mapped != null) result.add(mapped);
                }
            }
        }
        return result;
    }
}
