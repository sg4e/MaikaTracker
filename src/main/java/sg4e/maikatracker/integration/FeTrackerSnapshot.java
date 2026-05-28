package sg4e.maikatracker.integration;

import java.util.Collections;
import java.util.Set;
import sg4e.maikatracker.KeyItemMetadata;
import sg4e.ff4stats.fe.KeyItemLocation;

public class FeTrackerSnapshot {
    private final Set<KeyItemMetadata> found;
    private final Set<KeyItemMetadata> used;
    private final Set<KeyItemLocation> checkedLocations;

    public FeTrackerSnapshot(Set<KeyItemMetadata> found, Set<KeyItemMetadata> used, Set<KeyItemLocation> checkedLocations) {
        this.found = Collections.unmodifiableSet(found);
        this.used = Collections.unmodifiableSet(used);
        this.checkedLocations = Collections.unmodifiableSet(checkedLocations);
    }

    public Set<KeyItemMetadata> getFound() { return found; }
    public Set<KeyItemMetadata> getUsed() { return used; }
    public Set<KeyItemLocation> getCheckedLocations() { return checkedLocations; }
}
