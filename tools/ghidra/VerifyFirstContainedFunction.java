// Verify the proposed separation of the first contained LC_FUNCTION_STARTS entry.
// @category PS Touch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class VerifyFirstContainedFunction extends GhidraScript {

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
        println("PS TOUCH — FIRST CONTAINED FUNCTION CORRECTION PREFLIGHT");
        println("============================================================");

        boolean originalExists =
            original != null &&
            original.getEntryPoint().equals(originalEntry) &&
            original.getName().equals("FUN_00df81dc");

        boolean bodyCorrect = false;

        if (originalExists) {
            AddressRange[] ranges =
                new AddressRange[2];

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

        Instruction wrapperBranch =
            currentProgram.getListing().getInstructionAt(toAddr("00df81e4"));

        boolean wrapperCorrect =
            wrapperBranch != null &&
            wrapperBranch.getMnemonicString().equalsIgnoreCase("b") &&
            wrapperBranch.getMaxAddress().equals(toAddr("00df81e7"));

        Instruction candidateInstruction =
            currentProgram.getListing().getInstructionAt(candidateEntry);

        boolean candidateCorrect =
            candidateInstruction != null &&
            candidateInstruction.getMnemonicString().equalsIgnoreCase("push");

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(candidateEntry)) {
            if (reference.getFromAddress().equals(toAddr("01098b00")) &&
                reference.getReferenceType() == RefType.COMPUTED_JUMP) {
                incomingReference = true;
            }
        }

        boolean pass =
            originalExists &&
            bodyCorrect &&
            ownershipCorrect &&
            noIndependentFunction &&
            wrapperCorrect &&
            candidateCorrect &&
            incomingReference;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Original function correct : " + originalExists);
        println("Exact body ranges correct : " + bodyCorrect);
        println("Candidate ownership valid : " + ownershipCorrect);
        println("No independent function   : " + noIndependentFunction);
        println("Wrapper branch correct    : " + wrapperCorrect);
        println("Candidate prologue valid  : " + candidateCorrect);
        println("Incoming reference valid  : " + incomingReference);

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}