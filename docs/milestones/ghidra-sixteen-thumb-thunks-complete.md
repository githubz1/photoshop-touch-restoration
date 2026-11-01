# PS Touch — Sixteen ARM Thumb Thunks Reconstructed

## Milestone

We have reconstructed and independently verified all sixteen identified ARM Thumb veneers in Adobe Photoshop Touch's original ARMv7 executable.

**Result: PASS — 16/16 (100% of the identified veneer set).**

This work repairs Ghidra's representation of Adobe's original function boundaries and runtime relationships. It does not modify Adobe's executable instructions.

## What Was Wrong

Ghidra had attributed bytes belonging to several ARM Thumb veneers to unrelated functions, including Objective-C methods. As a result, the veneers were not consistently represented as independent thunk functions, even though the original executable already contained their instructions and displacement literals.

Our task was to recover the existing structure, not to substitute a new implementation.

## Reconstruction and Verification

We inspected each veneer and its surrounding function ownership, checked the original branch destination, applied guarded metadata corrections where necessary, reconstructed the eight-byte thunk, and independently inspected the result.

Each identified veneer occupies eight bytes of instructions followed by a four-byte displacement literal. The literal belongs outside the reconstructed thunk function body.

Independent verification covered six properties:

1. Ghidra recognizes the entry as a thunk.
2. Its function body is exactly eight bytes.
3. Its destination matches the original external target.
4. The four-byte displacement literal is excluded.
5. The original containing function retains its correct body.
6. Function ownership is correct and nonoverlapping.

**All sixteen independently verified reconstructions passed.** Individual milestone reports preserve the intermediate states and correction procedures for the first five veneers; not every veneer required an identical sequence of corrections.

## Completed Veneer Inventory

| # | Entry address | Result |
|---:|:---|:---|
| 1 | `01098b08` | PASS |
| 2 | `01098b14` | PASS |
| 3 | `01098b20` | PASS |
| 4 | `01098b2c` | PASS |
| 5 | `01098b38` | PASS |
| 6 | `01098b44` | PASS |
| 7 | `01098b50` | PASS |
| 8 | `01098b5c` | PASS |
| 9 | `01098b68` | PASS |
| 10 | `01098b74` | PASS |
| 11 | `01098b80` | PASS |
| 12 | `01098b8c` | PASS |
| 13 | `01098b98` | PASS |
| 14 | `01098ba4` | PASS |
| 15 | `01098bb0` | PASS |
| 16 | `01098bbc` | PASS |

These addresses form a contiguous sequence of twelve-byte records: eight-byte thunk instructions and a separate four-byte displacement literal.

### Verified targets for the final seven veneers

| # | Entry address | Original external target |
|---:|:---|:---|
| 10 | `01098b74` | `_objc_autoreleasePoolPop` |
| 11 | `01098b80` | `_close` |
| 12 | `01098b8c` | `_dispatch_queue_set_specific` |
| 13 | `01098b98` | `_dispatch_sync` |
| 14 | `01098ba4` | `_objc_getProperty` |
| 15 | `01098bb0` | `_objc_storeWeak` |
| 16 | `01098bbc` | `_objc_destroyWeak` |

The original verification scripts and earlier milestone documents remain the authoritative record for additional targets and function-specific details.

## Sixteenth Veneer: Final Independent Verification

The last veneer was reconstructed as an independent eight-byte thunk at **`01098bbc–01098bc3`**, branching to **`_objc_destroyWeak`** at **`EXTERNAL:00000186`**. Its four-byte displacement literal at **`01098bc4–01098bc7`** remains outside the function body.

The previously misidentified Objective-C `.cxx_destruct` function was restored to **`00faf87c–00faf88d`**, an eighteen-byte body. The independent inspection passed all six checks: thunk recognition, exact body length, original external destination, literal exclusion, preserved Objective-C function, and correct ownership.

The final verification was read-only. Adobe's executable bytes were not altered.

## Script Archival

The sixth through sixteenth reconstructions contributed **55 Ghidra Java scripts** to `tools/ghidra/`. Each archived copy was compared with its working original using SHA-256.

**Archival verification: 55/55 PASS.**

Together with the scripts already committed for the first five reconstructions, the directory contains **86 Java scripts**. The unrelated `InspectContainedLCFunctionStarts.java` was deliberately excluded from this archival batch.

## Scope of the Completion Figure

**ARM Thumb veneer reconstruction: 16/16 — 100%.** This is the completion rate for the sixteen identified veneers, not for the overall PS Touch restoration.

The separately established structural function recovery baseline was **60,059 matched starts out of 61,217 `LC_FUNCTION_STARTS` entries (98.11%)**, after Thumb-address normalization. That baseline was **not recalculated** during this milestone.

The wider work of restoring PS Touch on modern ARM64 iOS remains ongoing.

## Why This Matters

We've achieved this without rewriting any of Adobe's original instructions or developing replacement algorithms. Instead, we've carefully reconstructed the original function boundaries and runtime relationships, allowing Ghidra to recognize the implementation Adobe had already written. Throughout this process, every byte of the original executable has remained untouched.

The elegance is in what was already there. Each veneer is a compact eight-byte branch sequence, followed by the displacement literal that supports its original destination. Once Ghidra understands where the function starts, where it ends, and what it calls, the implementation remains beautifully simple.

The fascinating part is how far this trail has travelled. We started much earlier in Ghidra, reconstructing native ARMv7 functions from addresses, XREFs and nameless FUN_... routines. Then we crossed the native/ActionScript boundary, reconstructed the AIR extension interface, got the modern native implementation working, and now we're effectively doing archaeology on Adobe's AVM2 application framework itself.

**We've successfully repaired Ghidra's function metadata without altering Adobe's original machine code!**

## Final Status

- **Sixteen identified veneers reconstructed:** 16/16 — COMPLETE.
- **Independent veneer verifications:** 16/16 — PASS.
- **New Java scripts archived and SHA-256 verified:** 55/55 — PASS.
- **Original Adobe machine code:** UNCHANGED.
- **Overall PS Touch restoration:** ONGOING.

This report documents a completed Ghidra reconstruction milestone. It does not itself record a Git commit; staging, review, and commit are separate follow-up steps.
