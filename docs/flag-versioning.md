# Flag versioning and compatibility

## 1. Two unrelated version systems

MaikaTracker contains two version concepts that must not be confused:

| Version | Stored/derived where | Purpose |
| --- | --- | --- |
| **FF4FE flag specification version** | `FlagSet.getVersion()`, decoded by FF4StatsLib from readable or binary flags | Selects the valid flag vocabulary and the gameplay/UI compatibility behavior. |
| **MaikaTracker JSON state version** | `TrackerState.version`, currently `"1.0"` | Intended to describe the save-file schema. It currently has no migration dispatcher or validation logic. |

A JSON state version of `1.0` says nothing about whether the contained FF4FE flags are legacy `0.3.x` flags or newer flags.

## 2. Ownership boundary

Flag parsing is delegated to the sibling **FF4StatsLib** project. MaikaTracker imports `sg4e.ff4stats.fe.FlagSet`, stores one current instance in `MaikaTracker.flagset`, and queries it by exact flag name.

The dependency is configured as `../FF4StatsLib`, not as a versioned artifact. Consequently, the exact versions and parsing rules supported by a build are determined by the sibling checkout used at build time. MaikaTracker's own source defines the compatibility policy after parsing, but not the binary flag codec itself.

## 3. Applying flags

The Apply Flags action follows this sequence:

1. Trim the entire flags text area.
2. Call `FlagSet.from(text)`.
3. On success, store the returned `FlagSet` and normalize the text area to readable flags, optionally preceded by binary flags.
4. On `IllegalArgumentException`, display the parser message and set `flagset` to `null`.
5. Recompute shop visibility, accessible-location logic, and key-item count.

With the FF4StatsLib implementation expected by this project, `FlagSet.from` accepts:

- readable flag text;
- a URL containing flags or a binary seed identifier;
- binary flags, optionally with a seed suffix.

Do not implement a second parser in MaikaTracker. Add or correct flag-spec parsing in FF4StatsLib, then keep MaikaTracker's version-specific behavior at its UI/policy boundary.

## 4. The legacy/new classification

MaikaTracker intentionally reduces all parsed flag versions to two behavior families:

```java
public boolean newFlagset() {
    return flagset != null && !flagset.getVersion().startsWith("0");
}
```

- A version string beginning with `0` is treated as **legacy**.
- Any non-null version not beginning with `0` is treated as **new**.
- A null/invalid/unapplied flagset is **not new**.

This is a compatibility heuristic, not semantic-version comparison. It does not compare major/minor numbers and it has no explicit table of supported versions. A future non-`0` specification will automatically enter the new branch, even if it changes flag meanings.

The clearest example is `ShopPanel.UpdateFlags()`:

- the legacy branch is documented in code as flags `0.3.0` through `0.3.8` and queries names such as `Sc`, `Sx`, `Ps`, `S4`, `Ji`, `-nosirens`, and `-noapples`;
- the new branch is documented as `4.0.0+` and queries names such as `Scabins`, `Sempty`, `Pshop`, `Svanilla`, `Sshuffle`, `Sstandard`, `Spro`, `Swild`, `Sno:j`, and `Sno:sirens`.

## 5. Flag query helpers and their null semantics

`MaikaTracker` centralizes exact-name queries in `flagsetContains`, `flagsetContainsAll`, and `flagsetContainsAny`. They have an unusual but deliberate null policy.

### Default overloads

When `flagset == null`, the default query overloads return **true**:

- `flagsetContains("Kmain")` → true;
- `flagsetContainsAny("K", "Kmain")` → true;
- `flagsetContainsAll("K", "Pkey")` → true.

This makes the no-flags mode permissive: the tracker generally shows possibilities instead of hiding content it cannot disprove. Note that even `containsAll` returns true without flags, regardless of how many arguments it receives.

### `allowNullFlagset` overloads

The boolean overloads add an outer guard:

```text
(allowNullFlagset || flagset != null) && normalQuery(...)
```

Therefore:

| Call shape while `flagset == null` | Result |
| --- | --- |
| `flagsetContainsAny("A", "B")` | true |
| `flagsetContainsAny(true, "A", "B")` | true |
| `flagsetContainsAny(false, "A", "B")` | false |
| corresponding `contains`/`containsAll` forms | same policy |

The `false` form is used when a feature must only be enabled by an explicitly parsed flag, such as legacy `Sc`, `Sx`, `-nosirens`, and `-noapples` shop restrictions.

### Exact matching

Queries delegate to `FlagSet.contains` and use exact canonical names. There is no substring, alias, or case-insensitive matching in MaikaTracker. Compatibility aliases are expressed explicitly at call sites, for example:

- old `K` or new `Kmain`;
- old `Pk` or new `Pkey`;
- old `Ps` or new `Pshop`;
- old `Nk` or newer `Nkey`/`Knofree`;
- old `Kq` or new `Ksummon`;
- old `Km` or new `Kmoon`.

## 6. Where flags affect behavior

### Shops

`ShopPanel.UpdateFlags()` is the largest version split. It controls the visibility of item-category checkboxes according to legacy shop modes or newer tier/category flags. Shop checkbox names encode both a legacy category and a new-family tier/category, separated by commas; each version branch reads its own part.

### Key-item availability and default locations

`KeyItemPanel` uses flags to determine whether a key item may be assigned to a vanilla location, chest, or shop. On reset it also assigns deterministic locations when key-item randomization is off. Examples include:

- Package at Start;
- Sand Ruby at Antlion;
- Crystal at Kokkól for `V1`, at Objective for `Owin:crystal`, otherwise Zeromus;
- Twin Harp at Mist for no-free-key-item variants, otherwise Toroia;
- Pass availability/location according to legacy/new pass flags.

### Accessible-location logic

`MaikaTracker.updateLogic()` asks FF4StatsLib for generally accessible locations, then applies compatibility aliases and special gates. Summon and lunar locations use old/new aliases; Kokkól and Objective depend on win-condition flags; some locations depend on key-item/pass randomization modes.

### Party selection

`PartyLabel` supports both legacy character flags (`-start...`, `-no...`, `-only`, `-nodupes`) and newer names (`Cstart:...`, `Cno:...`, `Conly:...`, `Cnodupes`). `MaikaTracker.SetStartingMember()` likewise recognizes both starting-character vocabularies.

### XP behavior

The XP calculator uses specific legacy flags when present and otherwise treats a new-family flagset as enabling newer default XP behavior, except for explicit vanilla XP behavior. This is another place where the coarse `newFlagset()` classification has gameplay consequences.

## 7. Binary flags, readable flags, and seeds

The UI can display readable flags alone or binary and readable flags together. `FlagSet` remains the canonical in-memory representation.

When saving JSON:

1. `flagset.getBinary()` is split at the first `.`;
2. the portion before the dot is stored as `binary_flags`;
3. readable canonical/legacy-formatted output is stored as `text_flags`;
4. if the `FlagSet` has a seed, the suffix after the first dot is stored separately as `seed`.

When loading JSON, the input placed into the flags text area is selected in this precedence order:

1. if both text and binary flags are null, use `--NULL--` so applying/resetting produces no valid flagset;
2. if nonblank binary flags and seed exist, use `binary_flags + "." + seed`;
3. otherwise, if nonblank binary flags exist, use binary flags;
4. otherwise use readable text flags.

The load action then clicks Reset, and Reset first invokes Apply Flags. Thus all normal parsing, flag-dependent reset defaults, shop updates, and logic rebuilding happen before saved component state is restored.

## 8. JSON state versioning limitations

`TrackerState.version` defaults to `1.0`, and Jackson ignores unknown properties. However:

- the loader does not inspect `version`;
- there are no migration functions;
- missing collections or fields can still cause errors depending on the data;
- many per-entry restore operations catch and ignore exceptions;
- enum names, chest IDs, and shop names act as de facto schema identifiers.

If the JSON format changes, add explicit version validation/migration rather than only changing the default string.

## 9. Safe procedure for supporting a new FF4FE flag version

1. **Pin and inspect FF4StatsLib.** Confirm that it parses the version and maps readable/binary flags correctly.
2. **Decide the family deliberately.** Do not assume `!version.startsWith("0")` is sufficient if the new specification changes semantics.
3. **Inventory all aliases.** Search for `flagsetContains`, `newFlagset`, and literal flag names in `MaikaTracker`, `KeyItemPanel`, `PartyLabel`, and `ShopPanel`.
4. **Update version-sensitive policies.** Shops, party rules, key-item defaults, accessible locations, and XP rules are separate consumers.
5. **Test no-flags mode.** Preserve the intentional permissive/null behavior unless changing it is an explicit product decision.
6. **Test all representations.** Apply readable flags, binary flags, a seed URL, and a saved JSON state.
7. **Test reset and load.** Both paths reapply flags and can reveal defaults that direct UI testing misses.
8. **Document the new aliases/family.** Keep this file and the relevant code comments synchronized.

## 10. Common pitfalls

- Treating JSON `version: "1.0"` as the FF4FE flag version.
- Replacing the old/new heuristic with lexical string comparison; version strings are external identifiers, not safe decimal values.
- Calling a default flag helper when absence of flags should mean false. Use the `false` boolean overload in that case.
- Adding only the new spelling of a flag and breaking legacy flagsets.
- Assuming readable flags are what gets restored; binary flags take precedence in saved state.
- Forgetting that an unpinned FF4StatsLib checkout determines parsing behavior at build time.
