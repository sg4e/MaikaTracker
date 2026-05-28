package sg4e.maikatracker.autotracking;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.maikatracker.KeyItemMetadata;

public final class SniTrackerSnapshot {
    private final Set<KeyItemMetadata> found;
    private final Set<KeyItemMetadata> used;
    private final Set<KeyItemLocation> checkedLocations;
    private final Map<KeyItemMetadata, KeyItemLocation> foundLocations;

    public SniTrackerSnapshot(Set<KeyItemMetadata> found, Set<KeyItemMetadata> used, Set<KeyItemLocation> checkedLocations, Map<KeyItemMetadata, KeyItemLocation> foundLocations) {
        this.found = Collections.unmodifiableSet(found);
        this.used = Collections.unmodifiableSet(used);
        this.checkedLocations = Collections.unmodifiableSet(checkedLocations);
        this.foundLocations = Collections.unmodifiableMap(foundLocations);
    }

    public Set<KeyItemMetadata> getFound() { return found; }
    public Set<KeyItemMetadata> getUsed() { return used; }
    public Set<KeyItemLocation> getCheckedLocations() { return checkedLocations; }
    public Map<KeyItemMetadata, KeyItemLocation> getFoundLocations() { return foundLocations; }
}
