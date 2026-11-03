//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class CorrectThirdContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00f8099c");
        Address candidateEntry = toAddr("00f80cc4");
        Address candidateEnd = toAddr("00f80d19");
        Address nextEntry = toAddr("00f80d1c");
        Address branchAddress = toAddr("00f809a4");

        println("============================================================");
        println("PS TOUCH — THIRD CONTAINED FUNCTION METADATA CORRECTION");
        println("============================================================");

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

        boolean noIndependentFunction = candidate == null;

        Instruction branch =
            currentProgram.getListing()
                .getInstructionAt(branchAddress);

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

        Instruction originalReturn =
            currentProgram.getListing()
                .getInstructionAt(toAddr("00f809c0"));

        boolean originalReturnCorrect =
            originalReturn != null &&
            originalReturn.getMnemonicString().equalsIgnoreCase("bx") &&
            originalReturn.getMaxAddress().equals(toAddr("00f809c1"));

        boolean nextFunctionCorrect =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        boolean safe =
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

        println("\nSAFETY CHECKS");
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

        if (!safe) {
            println("\nRESULT : ABORTED");
            println("Database changes : NONE");
            return;
        }

        AddressSet correctedBody = new AddressSet();

        correctedBody.addRange(
            originalEntry,
            toAddr("00f809c1")
        );

        println("\nAPPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function corrected =
            currentProgram.getFunctionManager()
                .getFunctionAt(originalEntry);

        Function remainingOwner =
            currentProgram.getFunctionManager()
                .getFunctionContaining(candidateEntry);

        boolean correctedBodyValid =
            corrected != null &&
            corrected.getBody().getNumAddresses() == 38 &&
            corrected.getBody().getMinAddress().equals(originalEntry) &&
            corrected.getBody().getMaxAddress()
                .equals(toAddr("00f809c1"));

        boolean candidateFree =
            remainingOwner == null;

        boolean entireCandidateFree = true;

        for (Address address = candidateEntry;
             address.compareTo(candidateEnd) <= 0;
             address = address.next()) {

            if (currentProgram.getFunctionManager()
                    .getFunctionContaining(address) != null) {
                entireCandidateFree = false;
                break;
            }
        }

        boolean nextPreserved =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry) != null;

        Instruction branchAfter =
            currentProgram.getListing()
                .getInstructionAt(branchAddress);

        boolean branchPreserved =
            branchAfter != null &&
            branchAfter.getMnemonicString().equalsIgnoreCase("b.eq.w");

        println("Corrected body size : " +
            (corrected == null
                ? "NONE"
                : corrected.getBody().getNumAddresses()));

        println("Candidate owner     : " +
            (remainingOwner == null
                ? "NONE"
                : remainingOwner.getName()));

        println("\nPOST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Original body corrected : " + correctedBodyValid);
        println("Candidate ownership free : " + candidateFree);
        println("Entire candidate free    : " + entireCandidateFree);
        println("Conditional branch intact: " + branchPreserved);
        println("Next function preserved  : " + nextPreserved);

        boolean pass =
            correctedBodyValid &&
            candidateFree &&
            entireCandidateFree &&
            branchPreserved &&
            nextPreserved;

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}