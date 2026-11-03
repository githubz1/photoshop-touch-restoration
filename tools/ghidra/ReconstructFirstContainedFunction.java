// Reconstruct the first independently recovered LC_FUNCTION_STARTS function.
// @category PS Touch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;

public class ReconstructFirstContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address wrapperEntry = toAddr("00df81dc");
        Address functionEntry = toAddr("00df81e8");
        Address functionEnd = toAddr("00df8235");

        Listing listing = currentProgram.getListing();

        println("============================================================");
        println("PS TOUCH — FIRST CONTAINED FUNCTION RECONSTRUCTION");
        println("============================================================");

        Function wrapper =
            currentProgram.getFunctionManager().getFunctionAt(wrapperEntry);

        Function existing =
            currentProgram.getFunctionManager().getFunctionAt(functionEntry);

        Function owner =
            currentProgram.getFunctionManager().getFunctionContaining(functionEntry);

        boolean wrapperCorrect =
            wrapper != null &&
            wrapper.getBody().getNumAddresses() == 12 &&
            wrapper.getBody().getMinAddress().equals(wrapperEntry) &&
            wrapper.getBody().getMaxAddress().equals(toAddr("00df81e7"));

        boolean entryFree =
            existing == null && owner == null;

        boolean rangeFree = true;
        boolean instructionsValid = true;
        long instructionBytes = 0;

        AddressSet body = new AddressSet();
        body.addRange(functionEntry, functionEnd);

        for (AddressRange range : body) {

            Address cursor = range.getMinAddress();

            while (cursor.compareTo(range.getMaxAddress()) <= 0) {

                Function containing =
                    currentProgram.getFunctionManager().getFunctionContaining(cursor);

                if (containing != null) {
                    rangeFree = false;
                }

                Instruction instruction = listing.getInstructionAt(cursor);

                if (instruction == null ||
                    instruction.getMaxAddress().compareTo(functionEnd) > 0) {

                    instructionsValid = false;
                    break;
                }

                instructionBytes += instruction.getLength();
                cursor = instruction.getMaxAddress().next();
            }
        }

        boolean coverageCorrect =
            instructionsValid && instructionBytes == 78;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Wrapper corrected     : " + wrapperCorrect);
        println("Entry unowned         : " + entryFree);
        println("Entire range unowned  : " + rangeFree);
        println("Instructions valid    : " + instructionsValid);
        println("Instruction coverage  : " + coverageCorrect);

        if (!(wrapperCorrect &&
              entryFree &&
              rangeFree &&
              instructionsValid &&
              coverageCorrect)) {

            println("\nRESULT : ABORTED");
            return;
        }

        println("\nCREATING RECOVERED FUNCTION");
        println("------------------------------------------------------------");

        Function recovered =
            createFunction(functionEntry, "FUN_00df81e8");

        if (recovered == null) {
            println("RESULT : FAIL — Function creation returned null");
            return;
        }

        recovered.setBody(body);

        Function verified =
            currentProgram.getFunctionManager().getFunctionAt(functionEntry);

        boolean correct =
            verified != null &&
            verified.getEntryPoint().equals(functionEntry) &&
            verified.getBody().getNumAddresses() == 78 &&
            verified.getBody().getMinAddress().equals(functionEntry) &&
            verified.getBody().getMaxAddress().equals(functionEnd);

        println("Function name : " +
            (verified == null ? "NONE" : verified.getName()));

        println("Entry point   : " +
            (verified == null ? "NONE" : verified.getEntryPoint()));

        println("Body size     : " +
            (verified == null ? "NONE" : verified.getBody().getNumAddresses()));

        println("\nPOST-RECONSTRUCTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Recovered function correct : " + correct);

        println("\nRESULT : " + (correct ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}