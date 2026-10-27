# PS Touch — First Verified Ghidra Function Metadata Repair

> We’ve successfully repaired Ghidra’s function metadata without altering Adobe’s original machine code!

## Background

The `LC_FUNCTION_STARTS` audit identified **61,217** linker-recorded entries. After Thumb address normalization, **60,059** matched existing Ghidra functions, establishing a **98.11% structural coverage baseline** (not an overall PS Touch restoration percentage).

The audit identified **19** linker-recorded starts inside existing function bodies, including **16** contiguous ARM Thumb branch veneers. One veneer at `01098b08` was incorrectly included in the function body of `stop` at `00fb5bf0`.

## Scripts created and how they were used

All three scripts were run in Ghidra and copied into the Git repository under `tools/ghidra/`.

### 1. `InspectStopFunctionBody.java` — baseline inspection (read-only)

Inspected the `stop` function's body ranges and checked ownership of the veneer at `01098b08`.

Initial findings:

- Function: `stop`; entry: `00fb5bf0`
- Body size: **205 bytes** across **three ranges**
- Main range: `00fb5bf0–00fb5cb5` (**198 bytes**)
- Additional ranges: `01098b08–01098b08` (**1 byte**) and `01098b0a–01098b0f` (**6 bytes**)
- Veneer contained in `stop`: **true**
- Database changes: **none**

### 2. `VerifyStopFunctionBody.java` — preflight (read-only)

Checked the existing body against the exact expected baseline and tested the proposed corrected range, `00fb5bf0–00fb5cb5`, before any modification.

Preflight checks confirmed that the expected body matched, the entry and original contiguous body would remain, the veneer would be excluded, and the sizes would change from 205 to 198 bytes.

**Result: PASS. Database changes: none.**

### 3. `CorrectStopFunctionBody.java` — controlled correction

After saving the Ghidra program **and creating a separate project archive**, executed the correction script once. It checked the function name and exact original body before replacing the Ghidra function-body metadata with the verified 198-byte range.

Recorded execution:

```text
PS TOUCH — CONTROLLED FUNCTION-BODY CORRECTION
Function: stop
Original body: 205
Proposed body: 198
Correction verified: true
Veneer still owned by stop: false
RESULT: SUCCESS
Removed 7 incorrectly associated bytes.
Executable bytes modified: NONE
```

## Independent post-correction verification

Ran `InspectStopFunctionBody.java` again after the correction. Its read-only output confirmed:

```text
Function name : stop
Entry point   : 00fb5bf0
Body size     : 198

FUNCTION BODY RANGES
 1. 00fb5bf0 - 00fb5cb5 (198 bytes)
Total ranges : 1

Veneer address    : 01098b08
Contained in body : false
Owning function   : NONE
Database changes  : NONE
```

Thus the original method remained intact, the distant veneer was detached, and **no executable bytes were patched**. The repair changed Ghidra's analysis metadata, not Adobe's original machine code.

## Preservation and scope

- The Ghidra program was saved and a separate project archive created before the correction.
- The Git repository preserves the three Java scripts and this procedural record. **A Git commit does not itself contain the modified Ghidra database**; the Ghidra project/archive preserves that state.
- The detached veneer at `01098b08` has **not** yet been defined as a separate function or thunk.
- The remaining contained-entry anomalies have **not** been corrected in this milestone.
- Structural coverage remains at the **previously measured 98.11% baseline** until the classifier is rerun.

## Next investigations

Inspect the detached veneer independently; determine whether and how to define its function/thunk metadata; examine the other contained-entry anomalies one at a time; then rerun the structural classifier to obtain an updated measurement.
