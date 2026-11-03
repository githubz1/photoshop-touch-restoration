// Inspect LC_FUNCTION_STARTS entries located inside existing Ghidra functions.
// Read-only: does not modify the program database.
// @category PS Touch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;

public class InspectContainedLCFunctionStarts extends GhidraScript {
    private static final String TABLE_START = "014a0d3c";
    private static final int EXPECTED_ENTRIES = 61217;

    @Override
    public void run() throws Exception {
        Address cursor = toAddr(TABLE_START);
        Address base = currentProgram.getImageBase();
        long cumulativeOffset = 0;
        int processed = 0;
        int containedCount = 0;
        int instructionStarts = 0;
        int instructionInteriors = 0;
        int noInstruction = 0;

        println("LC_FUNCTION_STARTS — CONTAINED ENTRIES (READ ONLY)");
        println("Image base: " + base + " | Table: " + cursor);

        while (processed < EXPECTED_ENTRIES && !monitor.isCancelled()) {
            long value = 0;
            int shift = 0;
            int bytesRead = 0;
            int b;
            do {
                b = currentProgram.getMemory().getByte(cursor) & 0xff;
                value |= ((long) (b & 0x7f)) << shift;
                cursor = cursor.add(1);
                bytesRead++;
                shift += 7;
                if (bytesRead > 10) {
                    throw new IllegalStateException("Invalid ULEB128 at " + cursor);
                }
            } while ((b & 0x80) != 0);

            if (value == 0) {
                println("Terminator encountered after " + processed + " entries.");
                break;
            }

            cumulativeOffset += value;
            Address raw = base.add(cumulativeOffset);
            Address normalized = (raw.getOffset() & 1L) != 0L ? raw.subtract(1) : raw;
            processed++;

            Function exact = currentProgram.getFunctionManager().getFunctionAt(raw);
            Function aligned = currentProgram.getFunctionManager().getFunctionAt(normalized);
            if (exact != null || aligned != null) {
                continue;
            }

            Function containing = currentProgram.getFunctionManager().getFunctionContaining(normalized);
            if (containing == null) {
                continue;
            }

            containedCount++;
            Instruction ins = currentProgram.getListing().getInstructionContaining(normalized);
            String instructionPosition;
            if (ins == null) {
                noInstruction++;
                instructionPosition = "NO INSTRUCTION";
            } else if (normalized.equals(ins.getAddress())) {
                instructionStarts++;
                instructionPosition = "INSTRUCTION START";
            } else {
                instructionInteriors++;
                instructionPosition = "INSIDE INSTRUCTION";
            }

            int refs = 0;
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(normalized);
            while (it.hasNext()) {
                Reference ref = it.next();
                refs++;
            }

            println(String.format(
                "%2d. raw=%s normalized=%s function=%s entry=%s offset=+0x%x " +
                "instruction=%s mnemonic=%s position=%s refs=%d",
                containedCount, raw, normalized, containing.getName(),
                containing.getEntryPoint(), normalized.subtract(containing.getEntryPoint()),
                ins == null ? "NONE" : ins.getAddress().toString(),
                ins == null ? "NONE" : ins.getMnemonicString(), instructionPosition, refs));
        }

        println("------------------------------------------------------------");
        println("Entries processed          : " + processed);
        println("Inside existing functions  : " + containedCount);
        println("At instruction start       : " + instructionStarts);
        println("Inside instruction         : " + instructionInteriors);
        println("No instruction             : " + noInstruction);
        println("Contained checksum         : " +
            (instructionStarts + instructionInteriors + noInstruction));
        println("Database changes           : NONE");
        if (processed != EXPECTED_ENTRIES || containedCount != 19) {
            println("WARNING: Results differ from prior audit; inspect before drawing conclusions.");
        }
    }
}
