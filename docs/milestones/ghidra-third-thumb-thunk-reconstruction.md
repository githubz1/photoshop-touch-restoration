# PS Touch — Third ARM Thumb Thunk Reconstruction

## Milestone

Successfully reconstructed and independently verified the third original
ARM Thumb veneer in Adobe Photoshop Touch's ARMv7 executable.

**Result: PASS**

**Progress: 3 of 16 veneers reconstructed (18.75%).**

Adobe's original executable bytes remain unchanged.

## Original Objective-C Function

Function:
`setTipTypeDictionary:forPenAtIndex:withPeripheralWithUUIDString:`

Entry point: `00fb8948`

Initial Ghidra function body:

- `00fb8948–00fb8995` — 78 bytes
- `01098b20–01098b20` — 1 byte
- `01098b22–01098b27` — 6 bytes

Total: 85 bytes.

Ghidra had incorrectly associated seven bytes of the third ARM Thumb
veneer with the original Objective-C method.

The correct function body is:

`00fb8948–00fb8995` — 78 bytes.

The method ends with a branch through the veneer to `_objc_release`.

## Function-Body Correction

The initial read-only inspection confirmed the 85-byte function body.

The correction preflight passed all five checks.

The first correction attempt aborted because the function body no longer
matched the expected uncorrected state.

A subsequent inspection established that the function body had already
been reduced to the correct 78 bytes.

The precise operation responsible for that intervening change was not
established.

The final guarded correction script therefore aborted safely without
performing another modification.

An independent read-only inspection confirmed:

- Original function body: 78 bytes
- Original address range preserved
- Veneer no longer owned by the original function
- No independent veneer function present at that stage
- Displacement literal excluded

## Third ARM Thumb Veneer

Entry point: `01098b20`

Executable instruction range: `01098b20–01098b27`

Displacement literal: `01098b28–01098b2b`

Instruction sequence:

```asm
ldr.w r12, [DAT_01098b28]
add   r12, pc
bx    r12
```

Only the eight executable instruction bytes were included in the
reconstructed thunk.

The four-byte displacement literal remains outside its function body.

## Destination Identification

The veneer resolves to the Objective-C runtime function:

`/usr/lib/libobjc.A.dylib::_objc_release`

An existing import thunk was identified at:

`01099e70`

The read-only import inspection established:

- Function: `_objc_release`
- Existing import thunk size: 8 bytes
- Existing import recognized by Ghidra: true
- Immediate destination: `EXTERNAL:0000018f`
- Resolved destination: `EXTERNAL:0000018f`
- Destination is external: true

The separate `__arclite_objc_release` compatibility routine was
identified and excluded as the reconstruction target.

## Thunk Reconstruction

Using Ghidra's Create Thunk Function operation, an independent thunk
was created at:

`01098b20–01098b27`

Destination:

`EXTERNAL:0000018f`

Ghidra recognized the reconstructed function as `_objc_release`.

No executable bytes were modified.

## Independent Verification

Script: `InspectThirdReconstructedThunk.java`

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

- `InspectThirdThumbVeneer.java`
- `VerifyThirdThumbVeneer.java`
- `CorrectThirdThumbVeneer.java`
- `InspectObjcReleaseThunk.java`
- `InspectThirdReconstructedThunk.java`

All five repository copies were independently verified as byte-for-byte
identical to their corresponding Ghidra scripts.

## Reconstruction Progress

| Veneer | Entry | Destination | Status |
|---|---|---|---|
| 1 | `01098b08` | Internal Objective-C method | PASS |
| 2 | `01098b14` | C++ `operator.new` | PASS |
| 3 | `01098b20` | Objective-C `_objc_release` | PASS |

**Completed: 3/16 (18.75%)**

**Remaining: 13**

Next veneer: `01098b2c`

This percentage measures recovery of the identified ARM Thumb veneers,
not overall PS Touch application restoration progress.

The previously measured 98.11% structural function coverage remains
a baseline and has not been recalculated.

## Significance

Three independent reconstructions now demonstrate that the original
ARM Thumb veneer pattern can be recovered across internal Objective-C
methods, external C++ runtime functions, and external Objective-C
runtime functions.

The reconstruction restores Ghidra's understanding of Adobe's original
function boundaries and runtime linkage.

It does not replace or recompile the original ARMv7 instructions.

**Adobe's original machine code remains untouched.**
