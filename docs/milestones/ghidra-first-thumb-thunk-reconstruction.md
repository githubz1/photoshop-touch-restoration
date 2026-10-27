# PS Touch — First Independently Verified ARM Thumb Thunk Reconstruction

## Milestone

Recovered an independent Ghidra thunk function at `01098b08`, forwarding to the original Objective-C implementation `InternalJaJaControlConnection::setRunTo:` at `00f7c3f0`, without changing Adobe's executable bytes.

This work follows the separately documented function-body correction in commit `9c4eab4` (`docs/milestones/ghidra-function-metadata-repair.md`).

## Background

The `LC_FUNCTION_STARTS` audit recorded 61,217 entries and previously matched 60,059 entries after Thumb normalization (98.11% structural coverage). It identified 19 entries inside existing function bodies, including 16 contiguous linker-generated Thumb veneers. These numbers are the prior audit baseline, not a post-reconstruction recount.

The veneer at `01098b08` was previously incorrectly included in the `stop` function at `00fb5bf0`. `CorrectStopFunctionBody.java` removed seven unrelated bytes from Ghidra's `stop` function-body metadata. A subsequent read-only inspection verified that `stop` occupied `00fb5bf0–00fb5cb5` (198 bytes) and that no function owned `01098b08` before the thunk was created.

## Original veneer instructions

| Address | Content | Role |
| --- | --- | --- |
| `01098b08` | `ldr.w r12, [DAT_01098b10]` | Load branch displacement |
| `01098b0c` | `add r12, pc` | Calculate destination |
| `01098b0e` | `bx r12` | Branch to destination |
| `01098b10–01098b13` | Four-byte displacement literal | Data, excluded from function body |

The executable instruction range is **`01098b08–01098b0f` (8 bytes)**. The next veneer begins at `01098b14`.

## Independent target identification

1. Ghidra's branch reference at `01098b0e` resolved to `InternalJaJaControlConnection::setRunTo:`.
2. An Objective-C `method_t.imp` field at `0140abbc` contained the little-endian Thumb function pointer `00f7c3f1`.
3. Clearing the Thumb-state bit identified the implementation instruction address `00f7c3f0`.
4. Navigating to `00f7c3f0` confirmed the `setRunTo:` implementation and its references in Ghidra.

The pointer field address `0140abbc` is metadata, **not** the thunk destination.

## Ghidra reconstruction procedure

1. Navigate to `01098b08` in the Listing.
2. Select all three instructions, covering **8 addresses** from `01098b08` through `01098b0f` inclusive. Do not select the literal at `01098b10` or the next veneer at `01098b14`.
3. Right-click the selection and choose **Create Thunk Function**.
4. In the destination dialog, enter the independently verified implementation address **`00f7c3f0`**.
5. Confirm the creation and inspect the resulting `THUNK FUNCTION` at `01098b08`.

The initial attempt without a selection failed with “Must select thunk function body”; selecting the exact eight-byte instruction range resolved that issue.

## Verification script

**`tools/ghidra/InspectSetRunToThunk.java`** is a read-only Ghidra Java script. It obtains the function at `01098b08` and checks:

- Entry point and function name.
- `isThunk()` status.
- Body size and address ranges.
- `getThunkedFunction(false)` destination.
- Exclusion of the displacement literal at `01098b10`.

The script was run in Ghidra from `~/Admin/Ghidra/scripts/InspectSetRunToThunk.java`, copied to `tools/ghidra/InspectSetRunToThunk.java`, and the copies were confirmed byte-for-byte identical using `cmp`.

### Observed read-only verification output

```text
Function name    : setRunTo:
Entry point      : 01098b08
Is thunk         : true
Body size        : 8

FUNCTION BODY RANGES
1. 01098b08 - 01098b0f (8 bytes)

THUNK DESTINATION
Destination      : 00f7c3f0
Destination name : setRunTo:

VERIFICATION
Body correct     : true
Target correct   : true
Literal excluded : true
RESULT           : PASS
Database changes : NONE
```

`InspectStopFunctionBody.java` was also rerun after thunk creation, confirming that `stop` retained its single 198-byte body and that the new function at `01098b08` owned the veneer instead.

## Safety and preservation

- The changes concern **Ghidra function metadata**, not executable bytes.
- The earlier `stop` correction had been saved and backed up in a separate Ghidra project archive before modification.
- The new thunk definition exists in the **Ghidra program database**. Git-tracking this script and documentation alone does **not** capture the modified database; save the Ghidra program and maintain a current backup/archive as appropriate.
- Do not confuse the thunk function entry `01098b08` with the original implementation entry `00f7c3f0`.

## Follow-up

- Continue inspecting the remaining 15 veneers individually before defining additional thunks.
- Investigate the other contained `LC_FUNCTION_STARTS` entries separately; not all are necessarily errors.
- Rerun the complete structural classifier after the appropriate reconstruction batch to obtain a new measured coverage figure.

## Significance

This is the first veneer in the 16-entry sequence to have its independent function boundary, exact eight-byte body, and original destination verified in Ghidra. The reconstruction preserves Adobe's original ARM Thumb instructions and records a repeatable evidence-based procedure for subsequent veneers.
