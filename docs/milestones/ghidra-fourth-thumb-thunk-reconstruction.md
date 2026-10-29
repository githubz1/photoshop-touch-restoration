# PS Touch — Fourth ARM Thumb Thunk Reconstruction

## Milestone

Successfully reconstructed and independently verified the fourth
original ARM Thumb veneer in Adobe Photoshop Touch's ARMv7 executable.

**Result: PASS**

**Progress: 4 of 16 veneers reconstructed (25%).**

Adobe's original executable bytes remain unchanged.

## Original Objective-C Function

Function: `bitStringWithData:`

Entry point: `00fbd6a4`

Initial Ghidra function body:

- `00fbd6a4–00fbd7cf` — 300 bytes
- `01098b2c–01098b2c` — 1 byte
- `01098b2e–01098b33` — 6 bytes

Total: 307 bytes.

Ghidra incorrectly associated seven bytes of the fourth ARM Thumb
veneer with the original Objective-C method.

The corrected function body is:

`00fbd6a4–00fbd7cf` — 300 bytes.

## Function-Body Correction

The initial read-only inspection confirmed the 307-byte body.

An independent preflight verified all six expected conditions.

When the guarded correction script ran, its old-body and ownership
checks failed, so it aborted without modifying the database.

A subsequent read-only inspection established that the function
body had already been corrected to 300 bytes and that the veneer
was no longer owned by any function.

The precise operation responsible for the intervening metadata
change was not established.

The corrected function body was independently verified before
the fourth thunk was created.

## Fourth ARM Thumb Veneer

Entry point: `01098b2c`

Executable instruction range: `01098b2c–01098b33`

Displacement literal: `01098b34–01098b37`

Instruction sequence:

```asm
ldr.w r12, [DAT_01098b34]
add   r12, pc
bx    r12
```

The final instruction begins at `01098b32` and occupies bytes
`01098b32–01098b33`.

The reconstructed thunk contains exactly eight instruction bytes.

The four-byte displacement literal remains outside its body.

## Destination Identification

Runtime function:

`/usr/lib/libobjc.A.dylib::_objc_autoreleaseReturnValue`

Existing import thunk:

`01099dec`

Read-only inspection established:

- Function name: `_objc_autoreleaseReturnValue`
- Existing import thunk recognized by Ghidra: true
- Existing import thunk body size: 8 bytes
- Immediate destination: `EXTERNAL:00000184`
- Resolved destination: `EXTERNAL:00000184`
- Destination is external: true

The separate `__arclite_objc_autoreleaseReturnValue` compatibility
implementation was excluded as the reconstruction target.

## Thunk Reconstruction

Using Ghidra's Create Thunk Function operation, an independent
thunk was created at:

`01098b2c–01098b33`

Destination:

`EXTERNAL:00000184`

Ghidra recognized the reconstructed function as
`_objc_autoreleaseReturnValue`.

No executable bytes were modified.

## Independent Verification

Script: `InspectFourthReconstructedThunk.java`

Results:

- Thunk recognized: true
- Thunk body correct: true
- Destination correct: true
- Literal excluded: true
- Original body intact: true
- Ownership correct: true

**RESULT: PASS**

**Database changes during verification: NONE**

**Executable changes: NONE**

## Preserved Ghidra Scripts

- `InspectFourthThumbVeneer.java`
- `VerifyFourthThumbVeneer.java`
- `CorrectFourthThumbVeneer.java`
- `InspectObjcAutoreleaseReturnValueThunk.java`
- `InspectFourthReconstructedThunk.java`

All five repository copies were verified as byte-for-byte
identical to their corresponding Ghidra scripts.

## Reconstruction Progress

| Veneer | Entry | Destination | Status |
|---|---|---|---|
| 1 | `01098b08` | Internal Objective-C method | PASS |
| 2 | `01098b14` | C++ `operator.new` | PASS |
| 3 | `01098b20` | `_objc_release` | PASS |
| 4 | `01098b2c` | `_objc_autoreleaseReturnValue` | PASS |

**Completed: 4/16 (25%)**

**Remaining: 12**

Next veneer: `01098b38`

This percentage measures recovery of the identified ARM Thumb
veneers, not overall PS Touch application restoration progress.

The previously measured 98.11% structural function coverage
remains a baseline and has not been recalculated.

## Significance

Four original ARM Thumb veneers have now been reconstructed and
independently verified.

The fourth reconstruction confirms the original Objective-C
autorelease-return linkage while preserving the surrounding
function boundaries.

These reconstructions restore Ghidra's understanding of Adobe's
original function structure and runtime relationships.

They do not replace, recompile, or modify the original ARMv7
instructions.

**Adobe's original machine code remains untouched.**
