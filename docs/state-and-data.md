# State, imports, resources, and identifiers

## 1. Where state lives

MaikaTracker does not maintain one canonical run-state object during normal use. Current state is distributed across:

- `StativeLabel` icon indices in key-item, boss, and party widgets;
- key-item location/chest/shop references in `KeyItemPanel`;
- `MaikaTracker.locationsVisited`;
- chest labels inside `TreasureAtlas`;
- shop checkboxes in static `ShopPanel` collections;
- the current FF4StatsLib `FlagSet`;
- Swing controls and Java Preferences.

`TrackerState` is a serialization DTO assembled from those live objects only when saving.

## 2. JSON save format

`TrackerState` uses Jackson public fields and ignores unknown JSON properties. Default-valued booleans and null optional fields are omitted.

Top-level fields:

| Field | Meaning |
| --- | --- |
| `version` | Tracker JSON schema marker, currently `1.0`; not currently validated during load. |
| `text_flags` | Readable FF4FE flags from the current `FlagSet`. |
| `binary_flags` | Binary FF4FE flags without the dot-separated seed suffix. |
| `seed` | Seed suffix when present. |
| `keyItems` | Item enum name, location identifier, collected state, and used state. |
| `locationsVisited` | `KeyItemLocation.name()` values. |
| `bosses` | Boss identifier, slot/location identifier, seen state, and defeated state. |
| `characters` | Five `LevelData.name()` values or null entries. |
| `openedChests` | Stable treasure-atlas chest IDs. |
| `shopItems` | Shop name to checked-item-name lists. |

Key-item location strings are polymorphic identifiers: they may be a `KeyItemLocation.name()` or a chest ID. Shop location is intentionally not serialized in each key item because checked shop data handles it separately.

Saving uses a pretty-printing Jackson writer and stores the last save directory in Preferences.

## 3. JSON load sequence

Load order matters because later operations assume flags/defaults are already applied:

1. Deserialize JSON into `TrackerState`.
2. Choose flag input using binary+seed, then binary, then readable text precedence.
3. Click Reset, which first applies those flags and then resets all live state/defaults.
4. Restore key-item state and location strings.
5. Restore visited locations and progression side effects.
6. Rebuild logic.
7. Restore bosses and boss slots.
8. Restore five party entries.
9. Mark known chest IDs as opened.
10. Restore checked shop items.

Many individual restore blocks catch and ignore all exceptions. This provides partial forward/backward tolerance but can silently drop renamed/invalid identifiers. Top-level JSON read errors are logged and abort the load.

## 4. Identifier stability

The effective save schema depends on source-level identifiers:

- `KeyItemMetadata` enum names;
- FF4StatsLib `KeyItemLocation` enum names;
- `BossLabel` identifiers accepted by `valueOf`;
- FF4StatsLib `LevelData` enum names;
- treasure chest IDs such as `Z1` or `E6`;
- shop panel names and checkbox names.

Renaming any of these can break old saves or spoiler imports even if the Java types still compile. Add migration aliases when renaming persisted identifiers.

## 5. Spoiler-log importer

The spoiler importer accepts `.txt` files and expects the first line to be exactly the FF4FE spoiler-log title. It then scans for named sections and parses their content directly into live UI state:

- **Key Item Slots**: fixed-column location and item names; assigns key items to locations.
- **Characters**: fixed-column location and character; populates party/start information.
- **Battles**: fixed-column location and formation; maps spoiler formation names to boss labels.
- **Treasures**: section/map headings plus regular expressions for item and battle chests; updates maps and contents.
- **Shops**: shop headings and item patterns; updates shop checkboxes.

The parser is intentionally tailored to a specific text layout. Heading, column-width, or phrasing changes in FF4FE spoiler logs can break it. When changing it, test a representative spoiler log containing key items, characters, bosses, battle chests, ordinary chests, and shops.

## 6. Java Preferences versus run state

Preferences are machine/user settings and are not part of a JSON run save. They include:

- checked boss/key-item display behavior;
- checked-overlay darkness;
- reset-only behavior;
- autotracking enabled state;
- save and spoiler-log directories;
- configured colors.

This distinction matters when reproducing behavior: loading the same JSON on two machines can render differently or display used/defeated states differently because preferences control whether state `2` icons are used.

## 7. Resources

Runtime resources are loaded from the classpath through `MaikaTracker.loadResource` and `loadImageResource`.

| Directory | Use |
| --- | --- |
| `src/main/resources/key-items/color` and `grayscale` | Key-item states. |
| `src/main/resources/bosses/color` and `grayscale` | Boss states. |
| `src/main/resources/bosses/checkmark.png` | Checked/defeated overlay compositing. |
| `src/main/resources/characters` | Party sprites and unknown placeholder. |
| `src/main/resources/maps/**` | Dungeon-floor maps and other map assets. |

Resource filenames are referenced by code-generated strings and constants. Renaming an image can therefore compile successfully but fail at runtime. Keep color/grayscale pairs and expected naming patterns synchronized.

Some resources are third-party assets with licensing terms described in the root README. Do not assume the source-code license permits arbitrary redistribution of every image.

## 8. NetBeans form files

`MaikaTracker.form`, `LocationPanel.form`, and `ShopPanel.form` are GUI Builder source files. Corresponding Java methods contain marked generated regions. The root README requires changes to generated GUI code to remain compatible with NetBeans GUI Builder.

Recommended UI workflow:

1. edit layouts/components through NetBeans GUI Builder where practical;
2. keep custom logic outside generated regions;
3. review both `.form` and `.java` diffs;
4. build and manually launch the application;
5. verify saved identifier/component names if shops or event handlers changed.
