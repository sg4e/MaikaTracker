# MaikaTracker developer documentation

This directory documents MaikaTracker as it exists in this repository. It is intended for maintainers who need to change the application without first reverse-engineering its Swing form, FF4FE compatibility rules, save format, or SNI integration.

## Reading order

1. [Architecture](architecture.md) — the layers, control flow, ownership boundaries, and repository map.
2. [Flag versioning](flag-versioning.md) — how FF4FE flag specifications are parsed, classified, queried, persisted, and used for compatibility behavior.
3. [SNI autotracking](sni-autotracking.md) — the complete SNI communication flow, address-space handling, monitored memory layout, bit semantics, threading, and failure behavior.
4. [State, imports, and resources](state-and-data.md) — JSON state, spoiler-log imports, preferences, images, maps, and generated Swing forms.
5. [Build, test, and maintenance](development.md) — local setup, generated sources, tests, release flow, and change checklists.

## Important terminology

- **FF4FE**: Final Fantasy IV: Free Enterprise, the randomizer whose run state MaikaTracker displays.
- **Flag specification version**: the version encoded by an FF4FE flagset, such as the legacy `0.3.x` family or the newer family. This controls which flag names and meanings the tracker uses.
- **Tracker-state version**: the `version` field in a saved MaikaTracker JSON file. It is currently `1.0` and is independent of the FF4FE flag specification version.
- **SNI**: the gRPC service through which MaikaTracker discovers a connected emulator/device and reads game memory.
- **SNES A-bus address**: a CPU-visible 24-bit SNES address such as `$7E1500` or `$707080`. It is not necessarily the same numeric address used by a device backend.

## High-level data flow

```text
manual clicks / JSON state / spoiler log / SNI memory
                         |
                         v
                    MaikaTracker
       +-----------------+------------------+
       |                 |                  |
       v                 v                  v
 key-item/boss/party   logic model      maps and shops
 Swing components      (FF4StatsLib)    Swing components
       |                 |                  |
       +-----------------+------------------+
                         |
                         v
                rendered tracker window
```

MaikaTracker is deliberately stateful: most current run state lives directly in Swing component instances, with `MaikaTracker` coordinating updates. There is no separate application-wide immutable model. That fact is central to understanding save/load, autotracking, and testing.
