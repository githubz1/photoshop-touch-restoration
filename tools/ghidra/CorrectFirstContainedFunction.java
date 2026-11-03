// Correct the first contained LC_FUNCTION_STARTS function boundary.
// @category PS Touch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class CorrectFirstContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00df81dc");
        Address candidateEntry = toAddr("00df81e8");

        Function original =
            currentProgram.getFunctionManager().getFunctionAt(originalEntry);

        Function independent =
            currentProgram.getFunctionManager().getFunctionAt(candidateEntry);

        Function owner =
            currentProgram.getFunctionManager().getFunctionContaining(candidateEntry);

        println("============================================================");
        println("PS TOUCH — FIRST CONTAINED FUNCTION METADATA CORRECTION");
        println("============================================================");

        boolean originalCorrect =
            original != null &&
            original.getEntryPoint().equals(originalEntry) &&
            original.getName().equals("FUN_00df81dc");

        boolean bodyCorrect = false;

        if (originalCorrect) {
            AddressRange[] ranges = new AddressRange[2];
            int count = 0;

            for (AddressRange range : original.getBody()) {
                if (count < 2) {
                    ranges[count] = range;
                }
                count++;
            }

            bodyCorrect =
                count == 2 &&
                ranges[0].getMinAddress().equals(toAddr("00df81dc")) &&
                ranges[0].getMaxAddress().equals(toAddr("00df81e8")) &&
                ranges[1].getMinAddress().equals(toAddr("00df81ea")) &&
                ranges[1].getMaxAddress().equals(toAddr("00df8235")) &&
                original.getBody().getNumAddresses() == 89;
        }

        boolean ownershipCorrect =
            owner != null &&
            original != null &&
            owner.getEntryPoint().equals(original.getEntryPoint());

        boolean noIndependentFunction = independent == null;

        Instruction branch =
            currentProgram.getListing().getInstructionAt(toAddr("00df81e4"));

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b") &&
            branch.getMaxAddress().equals(toAddr("00df81e7"));

        Instruction prologue =
            currentProgram.getListing().getInstructionAt(candidateEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        boolean referenceCorrect = false;

        for (Reference reference : getReferencesTo(candidateEntry)) {
            if (reference.getFromAddress().equals(toAddr("01098b00")) &&
                reference.getReferenceType() == RefType.COMPUTED_JUMP) {
                referenceCorrect = true;
            }
        }

        boolean safe =
            originalCorrect &&
            bodyCorrect &&
            ownershipCorrect &&
            noIndependentFunction &&
            branchCorrect &&
            prologueCorrect &&
            referenceCorrect;

        println("\nSAFETY CHECKS");
        println("------------------------------------------------------------");
        println("Original function correct : " + originalCorrect);
        println("Exact body ranges correct : " + bodyCorrect);
        println("Candidate ownership valid : " + ownershipCorrect);
        println("No independent function   : " + noIndependentFunction);
        println("Wrapper branch correct    : " + branchCorrect);
        println("Candidate prologue valid  : " + prologueCorrect);
        println("Incoming reference valid  : " + referenceCorrect);

        if (!safe) {
            println("\nRESULT : ABORTED");
            println("Database changes : NONE");
            return;
        }

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(
            toAddr("00df81dc"),
            toAddr("00df81e7")
        );

        println("\nAPPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function corrected =
            currentProgram.getFunctionManager().getFunctionAt(originalEntry);

        Function remainingOwner =
            currentProgram.getFunctionManager().getFunctionContaining(candidateEntry);

        boolean correctedBodyValid =
            corrected != null &&
            corrected.getBody().getNumAddresses() == 12 &&
            corrected.getBody().getMinAddress().equals(toAddr("00df81dc")) &&
            corrected.getBody().getMaxAddress().equals(toAddr("00df81e7"));

        boolean candidateFree = remainingOwner == null;

        println("Corrected body size : " +
            (corrected == null ? "NONE" : corrected.getBody().getNumAddresses()));

        println("Candidate owner     : " +
            (remainingOwner == null ? "NONE" : remainingOwner.getName()));

        println("\nPOST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Original body corrected : " + correctedBodyValid);
        println("Candidate ownership free : " + candidateFree);

        println("\nRESULT : " +
            ((correctedBodyValid && candidateFree) ? "PASS" : "FAIL"));

        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}