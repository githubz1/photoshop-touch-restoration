# PS Touch — Contained Function Recovery Complete

**Milestone date:** 2 November 2026  
**Scope:** Contained `LC_FUNCTION_STARTS` anomalies in the current Ghidra database.

## Result

The final run of `InspectContainedLCFunctionStarts.java` examined **61,217** `LC_FUNCTION_STARTS` entries and reported **zero** entries contained within an existing function. The audit made no database changes.

```text
Entries processed          : 61217
Inside existing functions  : 0
At instruction start       : 0
Inside instruction         : 0
No instruction             : 0
Contained checksum         : 0
Database changes           : NONE
WARNING: Results differ from prior audit; inspect before drawing conclusions.
```

This completes the *contained-entry* recovery phase; it does **not** mean that all 61,217 function starts have been independently reconstructed or that all unmatched starts have been resolved.

## Relationship to earlier milestone

Sixteen ARM Thumb veneers were previously reconstructed and independently verified, as documented in `docs/milestones/ghidra-sixteen-thumb-thunks-complete.md` (commit `cce7f52`). The present milestone addresses three further contained entries. The relationship between the earlier audit's expected count of nineteen and these two recovery phases has not yet been independently reconciled.

## Three recovered functions

### 1. `FUN_00df81e8`

| Item | Verified result |
| --- | --- |
| Former owner | `FUN_00df81dc` |
| Corrected former-owner body | `00df81dc–00df81e7` (12 bytes) |
| Recovered entry | `00df81e8` |
| Recovered body | `00df81e8–00df8235` (78 bytes) |
| Original Thumb prologue | Preserved |
| Incoming reference | `01098b00 → 00df81e8` |
| Independent verification | **PASS**, six checks |

The original wrapper branch and the recovered routine's executable instructions were preserved.

### 2. `FUN_00f80a44`

| Item | Verified result |
| --- | --- |
| Former owner | `.cxx_destruct` at `00f7c6dc` |
| Corrected former-owner body | `00f7c6dc–00f7c6f7` (28 bytes) |
| Recovered entry | `00f80a44` |
| Recovered body | `00f80a44–00f80a6f` (44 bytes) |
| Incoming reference | `00f7c6f4 → 00f80a44` (`UNCONDITIONAL_JUMP`) |
| Following function | `00f80a70`, preserved |
| Independent verification | **PASS**, nine checks |

The original prologue and epilogue, tail branch, and neighboring function were preserved.

### 3. `FUN_00f80cc4`

| Item | Verified result |
| --- | --- |
| Former owner | `FUN_00f8099c` |
| Corrected former-owner body | `00f8099c–00f809c1` (38 bytes) |
| Recovered entry | `00f80cc4` |
| Recovered body | `00f80cc4–00f80d19` (86 bytes) |
| Incoming reference | `00f809a4 → 00f80cc4` (`CONDITIONAL_JUMP`) |
| Branch instruction | `b.eq.w 0x00f80cc4` |
| Original function return | `00f809c0  bx lr` |
| Recovered function return | `00f80d18  pop {r4,r5,r6,r7,pc}` |
| Following function | `FUN_00f80d1c`, preserved |
| Correction preflight | **PASS**, ten checks |
| Reconstruction preflight | **PASS**, nine checks |
| Reconstruction post-checks | **PASS**, three checks |

The conditional branch is consistent with a transfer to a separately recorded function entry. The reconstruction preserved that branch and both functions' original instructions. A separate, dedicated independent post-reconstruction verification script has **not yet** been run for this third function.

## Method and preservation

Each case was inspected before changing Ghidra metadata, checked against exact body ranges and references, then corrected and reconstructed. Post-correction checks confirmed ownership, function boundaries, and preservation of neighboring routines. The first two functions also passed dedicated independent verification scripts; the third passed the reconstruction script's post-checks.

**No Adobe executable bytes were changed.** The modifications were confined to Ghidra's function metadata.

Sixteen additional Ghidra Java scripts were archived under `tools/ghidra/`, bringing the reported repository total to **102**. The copy procedure reported **zero integrity mismatches**. These figures should be confirmed in the repository before the commit is finalized.

## Outstanding audit qualification

The contained-entry script still emits `WARNING: Results differ from prior audit; inspect before drawing conclusions.` Its historical expected count of nineteen needs comparison with the earlier audit methodology. The zero count is a verified result **for the current contained-entry audit**, not proof of global function-start coverage.

## Project significance

> We've successfully repaired Ghidra's function metadata without altering Adobe's original machine code!

Rather than replacing Adobe's algorithms, this work restores Ghidra's recognition of function boundaries and relationships in Adobe's existing ARMv7 executable.

> The fascinating part is how far this trail has travelled. We started much earlier in Ghidra, reconstructing native ARMv7 functions from addresses, XREFs and nameless FUN_... routines. Then we crossed the native/ActionScript boundary, reconstructed the AIR extension interface, got the modern native implementation working, and now we're effectively doing archaeology on Adobe's AVM2 application framework itself.

The broader PS Touch restoration continues, with remaining unmatched `LC_FUNCTION_STARTS` entries and the historical audit reconciliation still to investigate.
