# PS Touch — Second ARM Thumb Thunk Reconstruction

## Milestone

**Result:** The second of 16 identified contiguous ARM Thumb veneers was reconstructed in Ghidra and independently verified. The original executable bytes were not modified.

**Scope:** Ghidra function-body metadata and thunk definitions, not recompilation or runtime functionality.

## Starting evidence

The linker-generated veneer begins at `01098b14`:

| Address | Contents | Role |
| --- | --- | --- |
| `01098b14` | `ldr.w r12, [DAT_01098b1c]` | Load branch displacement |
| `01098b18` | `add r12, pc` | Compute target |
| `01098b1a` | `bx r12` | Branch |
| `01098b1c` | `0x00001168` | Literal, **not** part of the thunk body |

The caller `FUN_00f80bc0` branches at `00f80bd4` to the veneer. The next function starts at `00f80bd8`, supporting a contiguous 24-byte body for the caller (`00f80bc0–00f80bd7`).

## Original metadata anomaly

`InspectSecondThumbVeneer.java` reported a 31-byte function body for `FUN_00f80bc0`, split into three ranges:

```text
00f80bc0–00f80bd7  24 bytes  Original function
01098b14–01098b14   1 byte   Misassociated veneer fragment
01098b16–01098b1b   6 bytes  Misassociated veneer fragment
```

The missing `01098b15` byte explains why the whole veneer range did not satisfy the function-body `contains(start,end)` test, even though seven veneer bytes belonged to the caller. There was no independent function at `01098b14`, and the literal at `01098b1c` was excluded.

`VerifySecondThumbVeneer.java` independently checked this baseline and returned **PASS**, with no database modifications.

## Guarded function-body correction

`CorrectSecondThumbVeneer.java` checked the exact expected body ranges, function name, and absence of an independent veneer function before setting the corrected body to `00f80bc0–00f80bd7`.

```text
Original body : 31 bytes
Proposed body : 24 bytes
Body correct    : true
Veneer detached : true
RESULT: SUCCESS
```

A subsequent read-only rerun of `InspectSecondThumbVeneer.java` confirmed the original function now had one 24-byte range and that no function owned the veneer at `01098b14`.

## External C++ destination verification

The veneer resolves to `/usr/lib/libstdc++.6.dylib::operator.new(ulong)` (mangled symbol `__Znwm`), not the array allocator `operator.new[]` (`__Znam`).

`InspectOperatorNewThunk.java` inspected the existing import thunk at `01099c84`:

```text
Entry point           : 01099c84
Is thunk              : true
Body                  : 01099c84–01099c8b (8 bytes)
Immediate destination : EXTERNAL:00000166
Resolved destination  : EXTERNAL:00000166
Resolved is external  : true
```

The existing import thunk was used to establish the external function's representation in Ghidra; the recovered veneer was **not** arbitrarily redirected through `01099c84`.

## Reconstructed veneer thunk

Using **Create Thunk Function**, the eight instruction bytes `01098b14–01098b1b` were selected, excluding the literal at `01098b1c`. The destination was set to Ghidra's verified external function address `EXTERNAL:00000166`.

Ghidra subsequently displayed the recovered function as a thunk for `operator.new(ulong)`.

## Independent post-creation verification

`InspectSecondReconstructedThunk.java` reported:

```text
Function name       : operator.new
Entry point         : 01098b14
Is thunk            : true
Body size           : 8
Destination name    : operator.new
Destination address : EXTERNAL:00000166
Destination external: true

Thunk body correct  : true
Destination correct : true
Literal excluded    : true
Original body intact: true
Ownership correct   : true
RESULT             : PASS
Database changes   : NONE
```

The verifier was read-only. The earlier correction and thunk creation changed Ghidra's analysis metadata, not the original machine-code bytes.

## Milestone commentary

> We've now demonstrated that our reconstruction method works across different parts of Adobe's original executable, including both internal Objective-C functions and external C++ runtime imports.
>
> Two misplaced function boundaries have been corrected. Two original ARM Thumb veneers have been recovered. Both reconstructions have passed independent verification.
>
> The remarkable part is that we haven't needed to replace Adobe's implementation. We're restoring the structural information that allows Ghidra to understand what Adobe originally built.
>
> **The original machine code remains untouched.**

## Status and follow-up

- **Contiguous veneer batch:** 2 / 16 independently verified (**12.50%**); 14 remain.
- **Function-body repairs:** 2 independently verified (`stop` and `FUN_00f80bc0`).
- **Structural function coverage:** **98.11%** is the *previously measured baseline*, not a recalculated value and not overall PS Touch restoration progress.
- **Next:** Preserve the scripts and this report in Git, then investigate the third veneer at `01098b20` with read-only inspection before any changes.

### Associated Ghidra scripts

- `tools/ghidra/InspectSecondThumbVeneer.java`
- `tools/ghidra/VerifySecondThumbVeneer.java`
- `tools/ghidra/CorrectSecondThumbVeneer.java`
- `tools/ghidra/InspectOperatorNewThunk.java`
- `tools/ghidra/InspectSecondReconstructedThunk.java`
