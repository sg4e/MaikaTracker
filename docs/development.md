# Build, test, and maintenance guide

## 1. Prerequisites

- Java/JDK 8 or later; source and target compatibility are Java 8.
- A sibling checkout of `FF4StatsLib` at `../FF4StatsLib`.
- The included Gradle wrapper.
- NetBeans GUI Builder when editing generated Swing layouts.
- For live autotracking tests, an SNI gRPC server and compatible FF4FE ROM/device.

Expected directory layout:

```text
parent/
├── FF4StatsLib/
└── MaikaTracker/
```

## 2. Build commands

```sh
./gradlew test
./gradlew build
java -jar build/libs/MaikaTracker.jar
```

The protobuf plugin runs `protoc` and the gRPC Java generator from versions declared in `build.gradle`. Generated sources live under `build/generated/` and must not be hand-edited or committed.

The JAR task creates a fat executable JAR by unpacking runtime dependencies and includes the sibling FF4StatsLib JAR. Duplicate files are excluded.

## 3. Main dependencies

| Dependency | Purpose |
| --- | --- |
| FF4StatsLib sibling project | FF4FE flags, key-item logic, boss data, party/stat data. |
| Swing/JDK | Desktop UI and preferences. |
| Jackson Databind | JSON run-state save/load. |
| gRPC Netty/Protobuf/Stub | SNI communication and generated client types. |
| Glazed Lists | Autocomplete support in boss/position selectors. |
| Guava | General utility dependency. |
| Log4j/SLF4J binding | Application and SNI warning/error logging. |
| JUnit 4 | Tests. |

## 4. Tests

Current automated tests cover only part of the SNI layer:

- disabled poll does not emit;
- enabled poll reads/decodes/emits;
- read failure does not emit;
- basic found/used/checked bit decoding;
- key-item mapping keys are unique.

Notably absent are automated tests for:

- `SniGrpcMemoryReader` device selection, RPC requests, address modes, and fallback;
- found-location record decoding;
- the full checked-location mapping;
- flag-version compatibility and null semantics;
- JSON save/load round trips;
- spoiler-log imports;
- Swing UI and logic behavior.

Prefer extracting pure functions/policies and adding unit tests when changing these areas.

## 5. CI and release flow

The GitHub Actions workflow:

1. checks out MaikaTracker and FF4StatsLib as siblings;
2. installs Temurin Java 8;
3. runs `./gradlew test` for pull requests and pushes to `master`;
4. on `master`, builds the fat JAR;
5. creates a timestamp/SHA release and uploads the JAR;
6. generates build-provenance attestation.

Because FF4StatsLib is checked out without a pinned ref, CI/release behavior can change with its default branch. Investigate both repositories when a formerly green MaikaTracker commit begins failing.

## 6. Change checklists

### Flag behavior

- Read [Flag versioning](flag-versioning.md).
- Search all `flagsetContains*` and `newFlagset()` call sites.
- Test legacy, new, and null/no-flags modes.
- Test readable, binary, URL/seed, reset, and JSON-load paths.
- Verify shops, key-item defaults, logic, party rules, and XP behavior.

### SNI behavior

- Read [SNI autotracking](sni-autotracking.md).
- Confirm A-bus/raw/FxPakPro semantics and ROM-side data ownership.
- Update mappings and the documented address tables together.
- Add decoder and transport tests.
- Verify all-zero, unknown-bit, disconnect, and disabled-while-polling behavior.
- Keep UI changes on the Swing EDT.

### Persistence/imports

- Read [State and data](state-and-data.md).
- Preserve or migrate enum names, chest IDs, shop names, and checkbox names.
- Round-trip an old and a newly saved JSON file.
- Import a representative spoiler log.
- Verify reset defaults before and after restore.

### Swing/UI

- Preserve NetBeans GUI Builder compatibility.
- Avoid manual edits in generated blocks when possible.
- Verify icon state cycling, checked-icon preference behavior, reset, and colors.
- Launch the runnable JAR and inspect all affected tabs.

## 7. Documentation maintenance

Documentation should be changed in the same pull request when any of these change:

- monitored SNI addresses, lengths, mappings, address modes, or configuration;
- flag-family classification, aliases, or null semantics;
- JSON fields or persisted identifiers;
- project layers, dependencies, build layout, or release flow.

The SNI address tables are intentionally explicit. Treat a mismatch between them and `SniTrackerMappings`/`SniAutoTrackerService` as a defect.
