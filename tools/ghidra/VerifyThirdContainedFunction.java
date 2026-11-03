//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class VerifyThirdContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00f8099c");
        Address candidateEntry = toAddr("00f80cc4");
        Address candidateEnd = toAddr("00f80d19");
        Address nextEntry = toAddr("00f80d1c");

        Function original =
            currentProgram.getFunctionManager()
                .getFunctionAt(originalEntry);

        Function candidate =
            currentProgram.getFunctionManager()
                .getFunctionAt(candidateEntry);

        Function owner =
            currentProgram.getFunctionManager()
                .getFunctionContaining(candidateEntry);

        Function next =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry);

        println("============================================================");
        println("PS TOUCH — THIRD CONTAINED FUNCTION CORRECTION PREFLIGHT");
        println("============================================================");

        boolean originalCorrect =
            original != null &&
            original.getEntryPoint().equals(originalEntry) &&
            original.getName().equals("FUN_00f8099c");

        boolean bodyCorrect = false;

        if (originalCorrect) {

            AddressRange[] ranges = new AddressRange[3];
            int count = 0;

            for (AddressRange range : original.getBody()) {
                if (count < 3) {
                    ranges[count] = range;
                }
                count++;
            }

            bodyCorrect =
                count == 3 &&
                ranges[0].getMinAddress().equals(toAddr("00f8099c")) &&
                ranges[0].getMaxAddress().equals(toAddr("00f809c1")) &&
                ranges[1].getMinAddress().equals(toAddr("00f80cc4")) &&
                ranges[1].getMaxAddress().equals(toAddr("00f80cc4")) &&
                ranges[2].getMinAddress().equals(toAddr("00f80cc6")) &&
                ranges[2].getMaxAddress().equals(candidateEnd) &&
                original.getBody().getNumAddresses() == 123;
        }

        boolean ownershipCorrect =
            owner != null &&
            original != null &&
            owner.getEntryPoint().equals(originalEntry);

        boolean noIndependentFunction =
            candidate == null;

        Instruction branch =
            currentProgram.getListing()
                .getInstructionAt(toAddr("00f809a4"));

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b.eq.w") &&
            branch.getMaxAddress().equals(toAddr("00f809a7")) &&
            branch.getFallThrough().equals(toAddr("00f809a8"));

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(candidateEntry)) {
            if (reference.getFromAddress().equals(toAddr("00f809a4")) &&
                reference.getReferenceType() ==
                    RefType.CONDITIONAL_JUMP) {
                incomingReference = true;
            }
        }

        Instruction prologue =
            currentProgram.getListing()
                .getInstructionAt(candidateEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        Instruction epilogue =
            currentProgram.getListing()
                .getInstructionAt(toAddr("00f80d18"));

        boolean epilogueCorrect =
            epilogue != null &&
            epilogue.getMnemonicString().equalsIgnoreCase("pop") &&
            epilogue.getMaxAddress().equals(candidateEnd);

        Instruction returnInstruction =
            currentProgram.getListing()
                .getInstructionAt(toAddr("00f809c0"));

        boolean originalReturnCorrect =
            returnInstruction != null &&
            returnInstruction.getMnemonicString().equalsIgnoreCase("bx") &&
            returnInstruction.getMaxAddress()
                .equals(toAddr("00f809c1"));

        boolean nextFunctionCorrect =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        boolean pass =
            originalCorrect &&
            bodyCorrect &&
            ownershipCorrect &&
            noIndependentFunction &&
            branchCorrect &&
            incomingReference &&
            prologueCorrect &&
            epilogueCorrect &&
            originalReturnCorrect &&
            nextFunctionCorrect;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Original function correct : " + originalCorrect);
        println("Exact body ranges correct : " + bodyCorrect);
        println("Candidate ownership valid : " + ownershipCorrect);
        println("No independent function   : " + noIndependentFunction);
        println("Conditional branch valid  : " + branchCorrect);
        println("Incoming reference valid  : " + incomingReference);
        println("Candidate prologue valid  : " + prologueCorrect);
        println("Candidate epilogue valid  : " + epilogueCorrect);
        println("Original return valid     : " + originalReturnCorrect);
        println("Next function preserved   : " + nextFunctionCorrect);

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes : NONE");
        println("============================================================");
    }
}