//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class ReconstructThirdContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00f8099c");
        Address originalEnd = toAddr("00f809c1");

        Address candidateEntry = toAddr("00f80cc4");
        Address candidateEnd = toAddr("00f80d19");

        Address branchAddress = toAddr("00f809a4");
        Address nextEntry = toAddr("00f80d1c");

        Listing listing = currentProgram.getListing();

        println("============================================================");
        println("PS TOUCH — THIRD CONTAINED FUNCTION RECONSTRUCTION");
        println("============================================================");

        Function original =
            currentProgram.getFunctionManager()
                .getFunctionAt(originalEntry);

        Function existing =
            currentProgram.getFunctionManager()
                .getFunctionAt(candidateEntry);

        Function owner =
            currentProgram.getFunctionManager()
                .getFunctionContaining(candidateEntry);

        Function next =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry);

        boolean originalCorrect =
            original != null &&
            original.getName().equals("FUN_00f8099c") &&
            original.getEntryPoint().equals(originalEntry) &&
            original.getBody().getNumAddresses() == 38 &&
            original.getBody().getMinAddress().equals(originalEntry) &&
            original.getBody().getMaxAddress().equals(originalEnd);

        boolean entryFree =
            existing == null &&
            owner == null;

        boolean nextPreserved =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        Instruction branch =
            listing.getInstructionAt(branchAddress);

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b.eq.w") &&
            branch.getMaxAddress().equals(toAddr("00f809a7")) &&
            branch.getFallThrough() != null &&
            branch.getFallThrough().equals(toAddr("00f809a8"));

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(candidateEntry)) {

            if (reference.getFromAddress().equals(branchAddress) &&
                reference.getReferenceType() ==
                    RefType.CONDITIONAL_JUMP) {

                incomingReference = true;
            }
        }

        Instruction prologue =
            listing.getInstructionAt(candidateEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        Instruction epilogue =
            listing.getInstructionAt(toAddr("00f80d18"));

        boolean epilogueCorrect =
            epilogue != null &&
            epilogue.getMnemonicString().equalsIgnoreCase("pop") &&
            epilogue.getMaxAddress().equals(candidateEnd);

        boolean rangeFree = true;
        boolean instructionsValid = true;

        long instructionBytes = 0;

        Address cursor = candidateEntry;

        while (cursor.compareTo(candidateEnd) <= 0) {

            Function containing =
                currentProgram.getFunctionManager()
                    .getFunctionContaining(cursor);

            if (containing != null) {
                rangeFree = false;
            }

            Instruction instruction =
                listing.getInstructionAt(cursor);

            if (instruction == null ||
                instruction.getMaxAddress()
                    .compareTo(candidateEnd) > 0) {

                instructionsValid = false;
                break;
            }

            instructionBytes += instruction.getLength();

            cursor = instruction.getMaxAddress().next();

            if (cursor == null) {
                instructionsValid = false;
                break;
            }
        }

        boolean coverageCorrect =
            instructionsValid &&
            instructionBytes == 86 &&
            cursor != null &&
            cursor.equals(candidateEnd.next());

        boolean safe =
            originalCorrect &&
            entryFree &&
            nextPreserved &&
            branchCorrect &&
            incomingReference &&
            prologueCorrect &&
            epilogueCorrect &&
            rangeFree &&
            coverageCorrect;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Original function correct : " + originalCorrect);
        println("Candidate entry unowned   : " + entryFree);
        println("Entire range unowned      : " + rangeFree);
        println("Conditional branch valid  : " + branchCorrect);
        println("Incoming reference valid  : " + incomingReference);
        println("Candidate prologue valid  : " + prologueCorrect);
        println("Candidate epilogue valid  : " + epilogueCorrect);
        println("Instruction coverage      : " + coverageCorrect);
        println("Next function preserved   : " + nextPreserved);

        if (!safe) {
            println("\nRESULT : ABORTED");
            println("Database changes : NONE");
            println("============================================================");
            return;
        }

        println("\nCREATING RECOVERED FUNCTION");
        println("------------------------------------------------------------");

        Function recovered =
            createFunction(candidateEntry, "FUN_00f80cc4");

        if (recovered == null) {
            println("RESULT : FAIL — Function creation returned null");
            return;
        }

        AddressSet recoveredBody = new AddressSet();

        recoveredBody.addRange(candidateEntry, candidateEnd);

        recovered.setBody(recoveredBody);

        Function verified =
            currentProgram.getFunctionManager()
                .getFunctionAt(candidateEntry);

        boolean recoveredCorrect =
            verified != null &&
            verified.getEntryPoint().equals(candidateEntry) &&
            verified.getBody().getNumAddresses() == 86 &&
            verified.getBody().getMinAddress().equals(candidateEntry) &&
            verified.getBody().getMaxAddress().equals(candidateEnd);

        Function originalAfter =
            currentProgram.getFunctionManager()
                .getFunctionAt(originalEntry);

        boolean originalPreserved =
            originalAfter != null &&
            originalAfter.getBody().getNumAddresses() == 38 &&
            originalAfter.getBody().getMinAddress().equals(originalEntry) &&
            originalAfter.getBody().getMaxAddress().equals(originalEnd);

        boolean nextStillPreserved =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry) != null;

        println("Function name : " +
            (verified == null ? "NONE" : verified.getName()));

        println("Entry point   : " +
            (verified == null ? "NONE" : verified.getEntryPoint()));

        println("Body size     : " +
            (verified == null
                ? "NONE"
                : verified.getBody().getNumAddresses()));

        println("\nPOST-RECONSTRUCTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Recovered function correct : " + recoveredCorrect);
        println("Original function preserved: " + originalPreserved);
        println("Next function preserved    : " + nextStillPreserved);

        boolean pass =
            recoveredCorrect &&
            originalPreserved &&
            nextStillPreserved;

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}