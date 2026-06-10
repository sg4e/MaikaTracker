# SNI autotracking

## 1. Scope and guarantees

MaikaTracker's SNI layer is a **read-only polling integration**. Once per second while enabled, it reads four FF4FE-maintained memory ranges, converts their bitfields/records into MaikaTracker domain values, and applies a complete snapshot to the Swing UI.

It currently tracks:

- which key items have been found;
- which found key items have been used;
- which mapped key-item locations have been checked;
- the mapped location at which each of the first 17 key-item slots was found.

It does **not** currently track bosses, party composition, chest state, shops, flags, ROM identity, or the seed. It also does not write any value to the emulator/device.

## 2. Layered design

```text
SNI gRPC server / connected device
              |
              v
SniGrpcMemoryReader  -- transport, device selection, address spaces
              |
              v
SniAutoTrackerService -- scheduling and four fixed reads
              |
              v
SniTrackerDecoder     -- bit/record decoding through mappings
              |
              v
SniTrackerSnapshot    -- immutable-by-interface domain snapshot
              |
              v
MaikaTracker.applyAutoTrackerSnapshot -- EDT/UI/logic application
```

### Classes

| Class | Responsibility |
| --- | --- |
| `SniMemoryReader` | Minimal `read(int snesAddress, int length)` abstraction; enables fake readers in tests. |
| `SniGrpcMemoryReader` | SNI device discovery, address-mode choice, memory-mapping detection, gRPC request construction, exact-length validation, and channel shutdown. |
| `SniAutoTrackerService` | Scheduled poll loop, enable flag, fixed memory ranges, decoder/consumer connection, and throttled warnings. |
| `SniTrackerMappings` | Explicit ROM-side bit/index to MaikaTracker enum mapping. Unknown bits are intentionally absent. |
| `SniTrackerDecoder` | Generic little-endian bitfield decoding and two-byte found-location record decoding. |
| `SniTrackerSnapshot` | Carries found items, used items, checked locations, and found-item locations as unmodifiable collections. |

## 3. Configuration

| Environment variable | Default | Meaning |
| --- | --- | --- |
| `MAIKA_SNI_GRPC_TARGET` | `127.0.0.1:8191` | gRPC target. A plain `host:port` is passed to `ManagedChannelBuilder.forAddress`; resolver-style targets containing `:///` are passed to `forTarget`. |
| `MAIKA_SNI_DEVICE_URI` | unset | Exact SNI device URI to select. If absent, the first device advertising `ReadMemory` is selected; if none advertises it, the first listed device is selected anyway. |
| `MAIKA_SNI_ADDRESS_MODE` | `AUTO` | One of `AUTO`, `SNES_ABUS`, `RAW`, or `FXPAKPRO`, case-insensitive. Invalid values silently become `AUTO`. |

The gRPC channel uses plaintext. There is no TLS, authentication, per-call deadline, retry policy, or keepalive configuration in MaikaTracker.

The UI checkbox controls the service's volatile `enabled` flag and is persisted in Java Preferences. The scheduled task exists even while disabled, but returns without reading.

## 4. SNI protocol used by MaikaTracker

`src/main/proto/sni/sni.proto` is a local SNI protocol definition in package `com.github.alttpo.sni`. Gradle generates protobuf message classes and blocking gRPC stubs from it.

Although the schema declares device control, memory, filesystem, info, and NWA services, MaikaTracker calls only these RPCs:

1. **`Devices.ListDevices(DevicesRequest) -> DevicesResponse`**
   - Called on the first read only, unless constructing a new reader.
   - The request has no `kinds` filter.
   - The chosen `DevicesResponse.Device` is cached permanently for the reader's lifetime.

2. **`DeviceMemory.MappingDetect(DetectMemoryMappingRequest) -> DetectMemoryMappingResponse`**
   - Called when an A-bus request needs a ROM memory mapping and no usable mapping is cached.
   - The request supplies only the selected device URI.
   - `Unknown` is rejected as an `IOException`.
   - The resulting mapping is cached.

3. **`DeviceMemory.SingleRead(SingleReadMemoryRequest) -> SingleReadMemoryResponse`**
   - Called once for each monitored range, sequentially.
   - The outer request contains the selected device URI.
   - The inner `ReadMemoryRequest` contains request address, address space, optional memory mapping, and size.
   - Only `response.response.data` is consumed; response address metadata is not validated.
   - Returned data length must exactly equal requested length or the read fails.

MaikaTracker does not use `MultiRead` or `StreamRead`. Consequently, one logical poll is four independent gRPC reads and is not an atomic memory snapshot. The game can modify memory between reads.

### One poll on the wire

After device/mapping resolution, the regular poll is conceptually:

```text
SingleRead(uri, address=$7E1500, size=3)   -> found key-item bytes
SingleRead(uri, address=$7E1503, size=3)   -> used key-item bytes
SingleRead(uri, address=$7E1510, size=16)  -> checked-location bytes
SingleRead(uri, address=$707080, size=34)  -> found-location records
```

All four must succeed and decode before the consumer receives a snapshot. A failure in any read drops the entire poll.

## 5. Device discovery and caching semantics

Device selection occurs lazily on the first call to `read`:

1. call `ListDevices`;
2. fail if no devices exist;
3. if `MAIKA_SNI_DEVICE_URI` is set, require an exact URI match or fail;
4. otherwise select the first device whose capabilities include `ReadMemory`;
5. if no device advertises `ReadMemory`, select the first device anyway.

The selected device is cached. It is never re-listed or invalidated after disconnect, URI change, or read failure. Restarting MaikaTracker (or creating a new reader) is currently the recovery path for a stale cached device.

The reader's `read` method is synchronized, so concurrent callers cannot interleave device resolution or reads through one reader. The current poll service has only one worker anyway.

## 6. Address spaces and translation

The integer passed to `SniMemoryReader.read` is always authored as a **SNES A-bus address**. `SniGrpcMemoryReader` decides how to express that address to the selected backend.

### AUTO mode

| SNI device `kind` | Chosen mode |
| --- | --- |
| exactly `retroarch` | `RAW` |
| exactly `fxpakpro` | `FXPAKPRO` |
| anything else | `SNES_ABUS` |

Kind comparisons are case-sensitive exact comparisons.

### SNES_ABUS mode

The request sends the original 24-bit address with `AddressSpace.SnesABus` and the result of `MappingDetect`. This lets SNI translate CPU-visible addresses to the backend/device representation.

### RAW mode

The request sends the original numeric address unchanged with `AddressSpace.Raw` and no memory mapping. AUTO uses this for `retroarch`. MaikaTracker assumes that backend's raw addressing accepts the monitored numeric addresses as sent.

### FXPAKPRO mode

For work RAM addresses `$7E0000` through `$7FFFFF`, MaikaTracker converts to the FxPakPro address space:

```text
fxpakpro_address = $F50000 + (snes_address - $7E0000)
```

Examples:

| SNES A-bus | FxPakPro request |
| --- | --- |
| `$7E1500` | `$F51500` |
| `$7E1503` | `$F51503` |
| `$7E1510` | `$F51510` |

Addresses outside `$7E0000..$7FFFFF` cannot use this conversion. In particular, `$707080` is sent as `SnesABus` with a detected mapping even when the overall mode is FXPAKPRO.

For a translated FXPAKPRO read only, if every returned byte is zero, MaikaTracker retries the same logical address as `SnesABus` and returns the fallback if it contains any nonzero byte. If both are all zero, the primary all-zero result is retained. This heuristic helps backends that expose WRAM differently, but it also means an all-zero value causes an extra read and can be ambiguous when zero is the correct state.

### Why `$707080` is different

`$7E....`/`$7F....` are SNES work-RAM banks. `$707080` is outside that range and is treated by this code as a mapping-dependent A-bus address. The 34-byte data there behaves like persistent per-item location records. The project does not validate ROM/seed identity or document the ROM-side producer, so maintainers should describe it as the **FF4FE found-location table exposed at A-bus `$707080`**, not assume every ROM or mapping places generic SRAM there.

## 7. Monitored memory layout

All bitfields use **least-significant-bit first** indexing:

```text
global_bit_index = byte_index * 8 + bit_index
bit_index 0 corresponds to mask $01
bit_index 7 corresponds to mask $80
```

Unknown/unmapped set bits are ignored.

### Summary table

| A-bus start | Length | Shape | Meaning |
| --- | ---: | --- | --- |
| `$7E1500` | 3 bytes | 24-bit little bitfield | Found key items. Only indices 0–16 are mapped. |
| `$7E1503` | 3 bytes | 24-bit little bitfield | Used key items, in the same key-item order. Only indices 0–16 are mapped. |
| `$7E1510` | 16 bytes | 128-bit little bitfield | Checked location/event indices. Only explicitly mapped indices are consumed. |
| `$707080` | 34 bytes | 17 little-endian unsigned 16-bit records | For item slot `i`, the location/event index where that key item was found. |

The ranges do not overlap: found occupies `$7E1500..$7E1502`, used occupies `$7E1503..$7E1505`, checked occupies `$7E1510..$7E151F`, and found-location records occupy `$707080..$7070A1`.

## 8. Found and used key-item bit semantics

The same mapping applies to `$7E1500` and `$7E1503`. A set bit means the corresponding condition is true in that field. Importantly, **bit 0 is Package, not Crystal**.

| Global bit | Byte.bit | Key item |
| ---: | --- | --- |
| `0` | `0.0` | Package |
| `1` | `0.1` | Sand Ruby |
| `2` | `0.2` | Legend |
| `3` | `0.3` | Baron Key |
| `4` | `0.4` | Twin Harp |
| `5` | `0.5` | Earth |
| `6` | `0.6` | Magma Key |
| `7` | `0.7` | Tower Key |
| `8` | `1.0` | Hook |
| `9` | `1.1` | Luca Key |
| `10` | `1.2` | Darkness |
| `11` | `1.3` | Rat Tail |
| `12` | `1.4` | Adamant |
| `13` | `1.5` | Pan |
| `14` | `1.6` | Spoon |
| `15` | `1.7` | Pink Tail |
| `16` | `2.0` | Crystal |
| `17–23` | `2.1–2.7` | Unmapped/ignored |

The decoder does not enforce `used ⊆ found`; it faithfully emits both sets. The UI only displays a used/check state for an item if `found` is also set, because snapshot application chooses state zero when not found.

## 9. Checked-location bit semantics

The 16-byte field at `$7E1510` can represent global indices `0x00..0x7F`, but MaikaTracker maps only selected FF4FE event/location indices. The first mapped index is `0x20`, so the first four bytes currently contain no consumed bits.

| Global bit/index | Byte.bit | MaikaTracker location |
| ---: | --- | --- |
| `0x20` | `4.0` | START |
| `0x21` | `4.1` | ANTLION |
| `0x22` | `4.2` | FABUL |
| `0x23` | `4.3` | ORDEALS |
| `0x24` | `4.4` | BARON_INN |
| `0x25` | `4.5` | BARON_CASTLE |
| `0x26` | `4.6` | TOROIA |
| `0x27` | `4.7` | DARK_ELF |
| `0x28` | `5.0` | ZOT |
| `0x29` | `5.1` | TOP_BABIL |
| `0x2A` | `5.2` | LOW_BABIL |
| `0x2B` | `5.3` | DWARF_CASTLE |
| `0x2C` | `5.4` | SEALED_CAVE |
| `0x2D` | `5.5` | SUMMONED_MONSTERS_CHEST |
| `0x2E` | `5.6` | RAT_TAIL |
| `0x2F` | `5.7` | SHEILA_PANLESS |
| `0x30` | `6.0` | SHEILA_PAN |
| `0x31` | `6.1` | ASURA |
| `0x32` | `6.2` | LEVIATAN *(enum spelling)* |
| `0x33` | `6.3` | ODIN |
| `0x34` | `6.4` | SYLPH |
| `0x35` | `6.5` | BAHAMUT |
| `0x36` | `6.6` | PALE_DIM |
| `0x37` | `6.7` | WYVERN |
| `0x38` | `7.0` | PLAGUE |
| `0x39` | `7.1` | DLUNAR |
| `0x3A` | `7.2` | DLUNAR |
| `0x3B` | `7.3` | OGOPOGO |
| `0x59` | `11.1` | MIST |
| `0x5A` | `11.2` | ZEROMUS |
| `0x5D` | `11.5` | OBJECTIVE |

Two different ROM-side indices (`0x39` and `0x3A`) deliberately collapse to the one `DLUNAR` enum. Because decoded results are sets, either or both set bits produce one visited location.

When a snapshot is applied, `locationsVisited` is **cleared and replaced** by this mapped set. Manual visited-location entries not represented by a current mapped SNI bit are therefore removed on the next successful poll.

## 10. Found-location record semantics

The 34-byte range at `$707080` is decoded as 17 consecutive records:

```text
record i starts at offset i * 2
location_index = low_byte | (high_byte << 8)
item = KEY_ITEM_BY_BIT[i]
location = LOCATION_BY_BIT[location_index]
```

Thus record order follows key-item indices `0..16`:

```text
Package, Sand Ruby, Legend, Baron Key, Twin Harp, Earth,
Magma Key, Tower Key, Hook, Luca Key, Darkness, Rat Tail,
Adamant, Pan, Spoon, Pink Tail, Crystal
```

There is **no record for key-item index 17** because 34 bytes hold only 17 two-byte records. Unknown location indices and absent item mappings are ignored. A record is included in `foundLocations` even if the corresponding found bit is not set, but the UI only applies a found location inside its `if (found)` branch.

A location index uses the same numeric mapping table as checked-location bits; it is not a byte offset into the checked field. For example, bytes `20 00` decode to location index `0x0020`, which maps to `START`.

## 11. Polling, decoding, and failure semantics

`SniAutoTrackerService` creates a single-thread scheduled executor and schedules at a fixed rate. MaikaTracker supplies a 1000 ms initial delay and period.

A poll:

1. returns immediately if disabled;
2. performs the four reads sequentially;
3. decodes all values into sets/maps;
4. invokes the snapshot consumer on the scheduler thread.

Any exception from reading, decoding, or consuming aborts the poll. The service logs a warning at most once per five seconds, using only the exception message in the warning text. There is no user-visible connection status or backoff. Because calls have no deadlines, a stuck blocking RPC can also stall the sole scheduler thread.

`close()` stops the scheduler immediately. `SniGrpcMemoryReader.close()` separately shuts down its gRPC channel with a one-second graceful timeout, but the current application service is typed against `SniMemoryReader` and does not itself close the reader. Maintainers adding lifecycle handling should close both resources.

## 12. Snapshot application semantics

`MaikaTracker.applyAutoTrackerSnapshot` immediately queues work on the Swing EDT. The queued action rechecks the UI checkbox, so a snapshot polled just before the user disables tracking is discarded.

Key items without an SNI memory mapping (currently only **Pass**) are skipped entirely during snapshot application. Their icon state, location label, and shop/chest associations are never touched by autotracking. This means users can manually track Pass via the right-click context menu — setting it found, assigning a location, or placing it in a shop — and that state will persist across poll cycles. `SniTrackerMappings.AUTOTRACKABLE_KEY_ITEMS` defines which items are eligible for autotracking; any item absent from this set is treated as manual-only.

For every **autotrackable** key-item panel:

```text
not found                         -> state 0
found                             -> state 1
found + used + checked-icons on   -> state 2
found + used + checked-icons off  -> state 1
```

If an item is found and the snapshot has a mapped found location, `panel.setLocation(...)` is called. Existing locations are not explicitly cleared when a found-location record is absent, so stale/manual item locations can remain while the icon state is replaced.

After key items, the application replaces `locationsVisited`, updates the key-item count, and rebuilds accessible-location logic. No flags are inferred or changed by SNI.

## 13. Consistency limitations

- Four `SingleRead` calls are not atomic; fields may reflect slightly different game moments.
- A failed read discards the full poll, preserving the last UI state.
- The selected device and mapping are cached indefinitely.
- Unknown bits and indices are silently ignored.
- All-zero FXPAKPRO reads trigger an A-bus fallback even when zero is valid.
- There is no ROM hash, seed, or FF4FE version check before interpreting addresses.
- Autotracking is authoritative for item icon states and mapped visited locations on every successful poll, but only opportunistically updates item-location labels.
- Autotracking **skips key items that have no SNI memory mapping** (currently only Pass). These items are excluded from the snapshot decode entirely, so autotracking never touches their icon state or location. Users must track Pass manually using the right-click context menu, even when autotracking is enabled. This allows manual Pass tracking to coexist with autotracked items without being overwritten on every poll cycle.
- The snapshot wraps the supplied mutable collections with unmodifiable views rather than defensive copies. The current decoder does not mutate them after construction, but future producers must not do so.

## 14. Testing and extension checklist

When adding an address, bit, or backend behavior:

1. Confirm the ROM-side semantic and address space; distinguish A-bus, raw, and FxPakPro addresses.
2. Add an explicit mapping rather than relying on enum ordinal values.
3. Document byte order, bit order, length, and unmapped behavior.
4. Add decoder tests for the first/last mapped bits, unknown bits, and duplicate/collapsed mappings.
5. Add reader tests for request address space, mapping, exact length, and fallback behavior. The current suite does not cover `SniGrpcMemoryReader`.
6. Consider whether the new data should replace manual state or only enrich it.
7. Keep Swing mutation on the EDT.
8. If consistency matters across fields, prefer one `MultiRead` request or add a consistency/version marker on the ROM side.
9. Add device reconnect/cache invalidation if supporting long-running disconnect/reconnect cycles.
10. Update this address table in the same change.
