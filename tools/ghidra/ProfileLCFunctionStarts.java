// Read-only inspection of unmatched LC_FUNCTION_STARTS instruction addresses.
// Run with the cursor on the first defined ULEB128 item (014a0d3c).
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.util.Map;
import java.util.TreeMap;

public class ProfileLCFunctionStarts extends GhidraScript {
    @Override
    public void run() throws Exception {
        if (currentAddress == null) {
            println("ERROR: Navigate to the first ULEB128 item before running.");
            return;
        }
        Address cursor = currentAddress;
        Data first = currentProgram.getListing().getDefinedDataAt(cursor);
        if (!isULEB128(first)) {
            println("ERROR: No defined ULEB128 item starts at " + cursor);
            return;
        }

        long offset = 0;
        int total = 0, exact = 0, thumb = 0, inside = 0;
        int instructions = 0, undefined = 0, other = 0, thunks = 0;
        int examples = 0;
        int exactInstructionStarts = 0, insideInstructions = 0;
        int contiguous = 0, gap = 0, noPrevious = 0;
        int predecessorFallsThrough = 0, predecessorNoFallthrough = 0;
        Map<String, Integer> firstMnemonics = new TreeMap<>();
        println("LC_FUNCTION_STARTS READ-ONLY ENTRY PROFILE");
        println("Start: " + cursor + "  Image base: " + currentProgram.getImageBase());

        while (!monitor.isCancelled()) {
            Data item = currentProgram.getListing().getDefinedDataAt(cursor);
            if (!isULEB128(item)) break;
            long delta = decodeULEB128(cursor, item.getLength());
            if (delta == 0) break;
            offset += delta;
            Address raw = currentProgram.getImageBase().add(offset);
            Address normalized = ((raw.getOffset() & 1L) != 0L) ? raw.subtract(1) : raw;
            total++;

            Function found = currentProgram.getFunctionManager().getFunctionAt(raw);
            if (found != null) {
                exact++;
                if (found.isThunk()) thunks++;
            } else {
                Function aligned = currentProgram.getFunctionManager().getFunctionAt(normalized);
                if (!normalized.equals(raw) && aligned != null) {
                    thumb++;
                    if (examples < 12) {
                        println("THUMB MATCH " + raw + " -> " + normalized + " " + aligned.getName());
                        examples++;
                    }
                } else if (currentProgram.getFunctionManager().getFunctionContaining(normalized) != null) {
                    inside++;
                } else if (!currentProgram.getMemory().contains(normalized)) {
                    other++;
                } else if (currentProgram.getListing().getInstructionContaining(normalized) != null) {
                    instructions++;
                    Instruction instruction = currentProgram.getListing().getInstructionContaining(normalized);
                    boolean exactStart = instruction.getAddress().equals(normalized);
                    if (exactStart) exactInstructionStarts++;
                    else insideInstructions++;
                    String mnemonic = instruction.getMnemonicString().toLowerCase();
                    firstMnemonics.put(mnemonic,
                        firstMnemonics.getOrDefault(mnemonic, 0) + 1);
                    Instruction previous = currentProgram.getListing().getInstructionBefore(normalized);
                    String previousInfo = "none";
                    if (previous == null) {
                        noPrevious++;
                    } else {
                        boolean adjacent = previous.getMaxAddress().next().equals(normalized);
                        if (adjacent) contiguous++;
                        else gap++;
                        boolean fallsThrough = adjacent && normalized.equals(previous.getFallThrough());
                        if (fallsThrough) predecessorFallsThrough++;
                        else predecessorNoFallthrough++;
                        previousInfo = previous.getAddress() + ":" + previous.getMnemonicString()
                            + " adjacent=" + adjacent + " fallthrough=" + fallsThrough;
                    }
                    if (examples < 20) {
                        println("CANDIDATE " + (examples + 1) + " raw=" + raw
                            + " entry=" + normalized + " first=" + mnemonic
                            + " prev=" + previousInfo);
                        examples++;
                    }
                } else if (currentProgram.getListing().getUndefinedDataAt(normalized) != null) {
                    undefined++;
                } else {
                    other++;
                }
            }
            cursor = cursor.add(item.getLength());
        }

        int matched = exact + thumb;
        println("============================================================");
        println("CLASSIFICATION SUMMARY");
        println("============================================================");
        println("Entries processed          : " + total);
        println("Exact function matches     : " + exact);
        println("Thumb-normalized matches   : " + thumb);
        println("Inside existing functions  : " + inside);
        println("Disassembled instructions  : " + instructions);
        println("Undefined memory           : " + undefined);
        println("Other / unmapped           : " + other);
        println("Exact-match thunks         : " + thunks);
        println("Unmatched after normalize  : " + (total - matched));
        println("Corrected function matches : " + matched);
        if (total > 0) {
            println(String.format("Corrected coverage         : %.2f%%", 100.0 * matched / total));
        }
        println("Category checksum          : " + (exact + thumb + inside + instructions + undefined + other));
        println("Exact instruction starts   : " + exactInstructionStarts);
        println("Inside instructions        : " + insideInstructions);
        println("Instruction checksum       : " + (exactInstructionStarts + insideInstructions));
        println("Previous instruction adjacent: " + contiguous);
        println("Previous instruction gap     : " + gap);
        println("No previous instruction      : " + noPrevious);
        println("Adjacent fallthrough into entry: " + predecessorFallsThrough);
        println("No adjacent fallthrough      : " + predecessorNoFallthrough);
        println("FIRST-INSTRUCTION MNEMONIC COUNTS:");
        for (Map.Entry<String, Integer> entry : firstMnemonics.entrySet()) {
            println(String.format("  %-16s %d", entry.getKey(), entry.getValue()));
        }
        println("Database changes           : NONE");
    }

    private boolean isULEB128(Data data) {
        return data != null && data.getDataType().getName().toUpperCase().contains("ULEB128");
    }

    private long decodeULEB128(Address address, int length) throws Exception {
        long result = 0;
        int shift = 0;
        for (int i = 0; i < length; i++) {
            int b = currentProgram.getMemory().getByte(address.add(i)) & 0xff;
            result |= (long) (b & 0x7f) << shift;
            if ((b & 0x80) == 0) return result;
            shift += 7;
            if (shift >= 64) throw new Exception("ULEB128 exceeds 64 bits at " + address);
        }
        throw new Exception("Unterminated ULEB128 at " + address);
    }
}
