//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class VerifySecondContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00f7c6dc");
        Address candidateEntry = toAddr("00f80a44");
        Address nextEntry = toAddr("00f80a70");

        Function original =
            currentProgram.getFunctionManager().getFunctionAt(originalEntry);

        Function candidate =
            currentProgram.getFunctionManager().getFunctionAt(candidateEntry);

        Function owner =
            currentProgram.getFunctionManager().getFunctionContaining(candidateEntry);

        Function next =
            currentProgram.getFunctionManager().getFunctionAt(nextEntry);

        println("============================================================");
        println("PS TOUCH — SECOND CONTAINED FUNCTION CORRECTION PREFLIGHT");
        println("============================================================");

        boolean originalCorrect =
            original != null &&
            original.getEntryPoint().equals(originalEntry) &&
            original.getName().equals(".cxx_destruct");

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
                ranges[0].getMinAddress().equals(toAddr("00f7c6dc")) &&
                ranges[0].getMaxAddress().equals(toAddr("00f7c6f7")) &&
                ranges[1].getMinAddress().equals(toAddr("00f80a44")) &&
                ranges[1].getMaxAddress().equals(toAddr("00f80a44")) &&
                ranges[2].getMinAddress().equals(toAddr("00f80a46")) &&
                ranges[2].getMaxAddress().equals(toAddr("00f80a6f")) &&
                original.getBody().getNumAddresses() == 71;
        }

        boolean ownershipCorrect =
            owner != null &&
            original != null &&
            owner.getEntryPoint().equals(original.getEntryPoint());

        boolean noIndependentFunction = candidate == null;

        Instruction branch =
            currentProgram.getListing().getInstructionAt(toAddr("00f7c6f4"));

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b.w") &&
            branch.getMaxAddress().equals(toAddr("00f7c6f7"));

        Instruction prologue =
            currentProgram.getListing().getInstructionAt(candidateEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        Instruction epilogue =
            currentProgram.getListing().getInstructionAt(toAddr("00f80a6e"));

        boolean epilogueCorrect =
            epilogue != null &&
            epilogue.getMnemonicString().equalsIgnoreCase("pop") &&
            epilogue.getMaxAddress().equals(toAddr("00f80a6f"));

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(candidateEntry)) {
            if (reference.getFromAddress().equals(toAddr("00f7c6f4")) &&
                reference.getReferenceType() == RefType.UNCONDITIONAL_JUMP) {
                incomingReference = true;
            }
        }

        boolean nextFunctionCorrect =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        boolean pass =
            originalCorrect &&
            bodyCorrect &&
            ownershipCorrect &&
            noIndependentFunction &&
            branchCorrect &&
            prologueCorrect &&
            epilogueCorrect &&
            incomingReference &&
            nextFunctionCorrect;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Original function correct : " + originalCorrect);
        println("Exact body ranges correct : " + bodyCorrect);
        println("Candidate ownership valid : " + ownershipCorrect);
        println("No independent function   : " + noIndependentFunction);
        println("Tail branch correct       : " + branchCorrect);
        println("Candidate prologue valid  : " + prologueCorrect);
        println("Candidate epilogue valid  : " + epilogueCorrect);
        println("Incoming reference valid  : " + incomingReference);
        println("Next function preserved   : " + nextFunctionCorrect);

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("============================================================");
    }
}