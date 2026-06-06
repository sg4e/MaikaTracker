# Architecture

## 1. System overview

MaikaTracker is a Java 8 desktop application built with Swing and Gradle. Its main class, `sg4e.maikatracker.MaikaTracker`, is both the top-level `JFrame` and the application coordinator. It constructs the UI, owns shared services, reacts to input, performs logic updates, and serializes state.

The project is best understood as the following layers. These are conceptual layers rather than strictly enforced packages.

```text
+------------------------------------------------------------------+
| Application shell: MaikaTracker JFrame and NetBeans form          |
+-----------------------+----------------------+---------------------+
| Interactive widgets   | Run logic            | Data integration    |
| key items, bosses,    | accessibility, flags | JSON, spoiler logs, |
| party, shops, maps    | XP calculations      | preferences, SNI    |
+-----------------------+----------------------+---------------------+
| Project data/model helpers and FF4StatsLib domain objects          |
+------------------------------------------------------------------+
| Swing / Jackson / gRPC+protobuf / Glazed Lists / Log4j             |
+------------------------------------------------------------------+
```

## 2. Repository map

| Path | Responsibility |
| --- | --- |
| `src/main/java/sg4e/maikatracker/MaikaTracker.java` | Main window, composition root, event handlers, logic updates, state load/save, spoiler parsing, XP tools, preferences, and SNI snapshot application. |
| `src/main/java/sg4e/maikatracker/*.form` | NetBeans GUI Builder source. Generated sections in corresponding Java files must remain GUI Builder compatible. |
| `src/main/java/sg4e/maikatracker/autotracking/` | SNI transport abstraction, gRPC implementation, poll service, memory decoder, mappings, and immutable snapshot. |
| `src/main/proto/sni/sni.proto` | Local SNI protobuf/gRPC contract used to generate Java stubs. It declares more SNI services than MaikaTracker currently calls. |
| `src/main/resources/` | Runtime images for bosses, key items, characters, maps, and chest markers. |
| `src/test/java/sg4e/maikatracker/autotracking/` | Unit tests for poll enable/error behavior and basic bit decoding. |
| `build.gradle` | Java/application/protobuf plugins, dependencies, generated gRPC configuration, and fat-JAR assembly. |
| `settings.gradle` | Declares `FF4StatsLib` as a sibling Gradle project at `../FF4StatsLib`. |
| `.github/workflows/build_and_release.yml` | CI tests and timestamped release-JAR publication from `master`. |

## 3. Composition and startup

`MaikaTracker.main` installs the Nimbus look and feel when available and opens a `MaikaTracker` on the Swing event-dispatch thread (EDT). The constructor is the composition root:

1. It initializes NetBeans-generated Swing components.
2. It opens the application's `java.util.prefs.Preferences` node.
3. It creates an `SniGrpcMemoryReader`, `SniTrackerDecoder`, and `SniAutoTrackerService`.
4. It populates party, boss, map, shop, and key-item widgets.
5. It restores visual and behavioral preferences.
6. It enables or disables the SNI poller according to the saved preference.

The SNI service creates its scheduler immediately, but polling is a no-op until enabled. See [SNI autotracking](sni-autotracking.md).

## 4. Application shell and coordination layer

`MaikaTracker` is the central coordinator and a globally reachable singleton through `MaikaTracker.tracker`. Child classes use either this static reference or `getTrackerFromChild` to reach the frame. Its responsibilities include:

- constructing and navigating all tabs and panels;
- owning the current `FlagSet`, `TreasureAtlas`, visited-location set, and SNI service;
- translating flags into UI visibility and logic behavior;
- coordinating key-item locations, chests, shops, and the accessible-location panel;
- saving/loading JSON and importing FF4FE spoiler logs;
- maintaining user preferences such as colors, directories, checked-icon behavior, and autotracking enablement;
- calculating party XP and boss information through FF4StatsLib.

This makes changes convenient but tightly coupled. A change to a child widget can have effects in logic, persistence, and imports because the visible widget state is frequently the source of truth.

## 5. Interactive presentation layer

### 5.1 Stateful labels

`StativeLabel` is the base interaction primitive. It owns an ordered list of icons and an integer icon index. A left click cycles through states. `setActive`, `setState`, and `reset` mutate the same index programmatically. Most tracker state is represented by these icon indices:

- index `0`: unseen/not found/inactive;
- index `1`: seen/found/active;
- index `2`: checked/defeated/used when the widget has a checked icon.

`DemoLabel` is a non-clickable legend that demonstrates gray, color, and checked states.

### 5.2 Key items

`KeyItemMetadata` binds each FF4FE key item to:

- its FF4StatsLib `KeyItem` enum;
- a spoiler-log name;
- gray/color/checked icons.

`KeyItemPanel` combines a stateful item icon and a location label. It can associate a key item with exactly one of:

- a `KeyItemLocation`;
- a `ChestLabel` in the treasure atlas;
- a `ShopPanel`.

Its reset behavior also applies flag-dependent vanilla locations and removes unavailable items. Its context menus let users assign locations, chests, or shops. Because location assignment updates `locationsVisited` and logic, it is not merely presentational.

### 5.3 Bosses

`BossLabel` represents a boss slot/state and supports gray, color, and optionally checked icons. It also carries the boss/slot association used by boss-stat displays and persistence. The class includes image compositing for checked overlays. Boss metadata and calculations come from FF4StatsLib's `Battle` and `Formation` classes.

### 5.4 Party and XP

`PartyLabel` represents one of five party slots and gates available characters according to flags and progression (`MtOrdealsComplete`, `DwarfCastleComplete`). It owns a FF4StatsLib `LevelData`/`PartyMember` relationship.

`PartyTableModel` adapts party members to the XP table. XP calculations and the D. Machin helper are coordinated by `MaikaTracker` and use FF4StatsLib party/stat objects.

### 5.5 Shops

Each `ShopPanel` contains categorized checkboxes. Static collections allow all shops to be updated, reset, recolored, serialized, or restored together. `ShopPanel.UpdateFlags()` has separate branches for legacy and newer FF4FE flag vocabularies. Checkbox `name` metadata encodes categories/tier rules used by this method.

### 5.6 Treasure maps

- `TreasureChest` is an immutable chest ID plus map coordinate.
- `ChestLabel` is the clickable map marker and can hold a key item.
- `TreasureMap` renders one floor image and its chest markers.
- `TreasureAtlas` indexes maps by dungeon/floor and chest ID, supports navigation, and serializes opened chest IDs.

Chest IDs are also used by spoiler-log parsing and key-item persistence, so they form a small stable identifier scheme within the application.

### 5.7 Logic panel

The accessible-location panel is rebuilt by `MaikaTracker.updateLogic()`:

1. collect acquired key-item enums from active key-item panels;
2. ask `KeyItemLocation.getAccessibleLocations(...)` in FF4StatsLib for candidate locations;
3. remove `locationsVisited`;
4. apply MaikaTracker-specific flag gates for summon, lunar, free-item, Fabul, Baron, Kokkól/objective, and Zeromus cases;
5. create clickable `LocationPanel` entries.

Completing a location adds it to `locationsVisited`, removes its panel, and updates special party progression state for Mt. Ordeals and Dwarf Castle.

## 6. Domain/model layer and FF4StatsLib boundary

MaikaTracker depends on a sibling Gradle project named `FF4StatsLib`. It is not pinned to a Maven artifact or commit by this repository. The sibling provides the domain knowledge that would otherwise be expensive to duplicate:

- FF4FE flag parsing and flag-version definitions (`FlagSet`);
- key-item accessibility (`KeyItem`, `KeyItemLocation`);
- boss battle/formations and stats (`Battle`, `Formation`);
- party members, levels, equipment, and XP-related data.

MaikaTracker adds UI metadata and compatibility policy around those objects. For example, FF4StatsLib determines broadly accessible key-item locations, while `MaikaTracker.updateLogic()` removes already-visited locations and applies tracker-specific flag gates.

**Maintenance implication:** because `settings.gradle` points at `../FF4StatsLib`, two developers can build the same MaikaTracker commit against different FF4StatsLib revisions. Flag parsing and domain behavior can therefore change without a MaikaTracker source change. CI checks out the current FF4StatsLib default branch as well. For reproducible releases, pinning that dependency would be necessary.

## 7. Integration/data layer

### JSON state

`TrackerState` is a Jackson DTO. Saving walks live Swing components and supporting collections to create a snapshot. Loading first applies flags/reset behavior, then mutates widgets and collections from the DTO. See [State and data](state-and-data.md).

### Spoiler logs

The spoiler importer is a positional text parser in `MaikaTracker`. It recognizes specific section headings and fixed-width/content patterns for key-item slots, characters, battles, treasures, and shops. It directly updates the same widgets used by manual tracking.

### Preferences

`java.util.prefs.Preferences` stores user settings outside the JSON run state. These include colors, previously used directories, reset behavior, checked-icon behavior/darkness, and whether autotracking is enabled.

### SNI

The autotracking package is the only network/device integration. It reads four memory ranges, decodes them into project domain values, and sends snapshots to the Swing coordinator. It is read-only: the protobuf contract declares writes, but MaikaTracker never writes to SNI.

## 8. Threading model

- Swing event handlers and manual state changes run on the EDT.
- `SniAutoTrackerService` polls on one scheduled-executor thread.
- The poller decodes a complete snapshot off the EDT.
- `MaikaTracker.applyAutoTrackerSnapshot()` uses `SwingUtilities.invokeLater` before reading or mutating Swing state.

The current architecture assumes that non-SNI state mutations originate on the EDT. New integrations should preserve this boundary.

## 9. Dependency and generated-code layer

Gradle generates Java protobuf messages and gRPC stubs from `src/main/proto/sni/sni.proto`. The application uses blocking stubs over gRPC Netty. The build creates a fat JAR by unpacking runtime dependencies and the sibling FF4StatsLib JAR.

The `.form` files are NetBeans GUI Builder definitions. Sections marked as generated in Java should not be hand-edited unless the result remains compatible with the GUI Builder.

## 10. Architectural risks and extension points

- **Central coordinator:** `MaikaTracker` is large and owns unrelated concerns. Prefer extracting pure decoders/policies rather than adding more parsing to it.
- **Widget-as-model:** programmatic state changes must update all derived UI/logic state, not just an icon.
- **Static/global state:** boss lists, party lists, shops, and `MaikaTracker.tracker` make isolated tests difficult.
- **External unpinned domain library:** validate behavior against the actual sibling FF4StatsLib revision used for a release.
- **Identifier stability:** enum names, chest IDs, and shop names appear in persisted/imported data. Renaming them requires migration or compatibility handling.
- **SNI mapping tables:** new ROM-side bits are ignored until explicitly mapped. This is safer than guessing, but requires coordinated updates.
