//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class InspectSecondRecoveredFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address destructorEntry = toAddr("00f7c6dc");
        Address recoveredEntry = toAddr("00f80a44");
        Address recoveredEnd = toAddr("00f80a6f");
        Address nextEntry = toAddr("00f80a70");
        Address branchAddress = toAddr("00f7c6f4");

        Function destructor =
            currentProgram.getFunctionManager()
                .getFunctionAt(destructorEntry);

        Function recovered =
            currentProgram.getFunctionManager()
                .getFunctionAt(recoveredEntry);

        Function next =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry);

        println("============================================================");
        println("PS TOUCH — SECOND RECOVERED FUNCTION VERIFICATION");
        println("============================================================");

        boolean destructorCorrect =
            destructor != null &&
            destructor.getName().equals(".cxx_destruct") &&
            destructor.getEntryPoint().equals(destructorEntry) &&
            destructor.getBody().getNumAddresses() == 28 &&
            destructor.getBody().getMinAddress()
                .equals(destructorEntry) &&
            destructor.getBody().getMaxAddress()
                .equals(toAddr("00f7c6f7"));

        boolean recoveredCorrect =
            recovered != null &&
            recovered.getName().equals("FUN_00f80a44") &&
            recovered.getEntryPoint().equals(recoveredEntry) &&
            recovered.getBody().getNumAddresses() == 44 &&
            recovered.getBody().getMinAddress()
                .equals(recoveredEntry) &&
            recovered.getBody().getMaxAddress()
                .equals(recoveredEnd);

        boolean ownershipCorrect =
            recovered != null &&
            currentProgram.getFunctionManager()
                .getFunctionContaining(recoveredEntry) == recovered &&
            currentProgram.getFunctionManager()
                .getFunctionContaining(recoveredEnd) == recovered;

        boolean independent =
            destructor != null &&
            recovered != null &&
            !destructor.getBody().intersects(recovered.getBody());

        Instruction branch =
            currentProgram.getListing()
                .getInstructionAt(branchAddress);

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b.w") &&
            branch.getMaxAddress().equals(toAddr("00f7c6f7"));

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(recoveredEntry)) {
            if (reference.getFromAddress().equals(branchAddress) &&
                reference.getReferenceType() ==
                    RefType.UNCONDITIONAL_JUMP) {
                incomingReference = true;
            }
        }

        Instruction prologue =
            currentProgram.getListing()
                .getInstructionAt(recoveredEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        Instruction epilogue =
            currentProgram.getListing()
                .getInstructionAt(toAddr("00f80a6e"));

        boolean epilogueCorrect =
            epilogue != null &&
            epilogue.getMnemonicString().equalsIgnoreCase("pop") &&
            epilogue.getMaxAddress().equals(recoveredEnd);

        boolean nextPreserved =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        println("\nFUNCTIONS");
        println("------------------------------------------------------------");

        println("Destructor entry : " + destructorEntry);
        println("Destructor size  : " +
            (destructor == null
                ? "NONE"
                : destructor.getBody().getNumAddresses()));

        println("Recovered entry  : " + recoveredEntry);
        println("Recovered size   : " +
            (recovered == null
                ? "NONE"
                : recovered.getBody().getNumAddresses()));

        println("Next entry       : " + nextEntry);

        println("\nVERIFICATION");
        println("------------------------------------------------------------");
        println("Destructor body correct : " + destructorCorrect);
        println("Recovered body correct  : " + recoveredCorrect);
        println("Ownership correct       : " + ownershipCorrect);
        println("Functions independent   : " + independent);
        println("Tail branch preserved   : " + branchCorrect);
        println("Incoming reference      : " + incomingReference);
        println("Original prologue       : " + prologueCorrect);
        println("Original epilogue       : " + epilogueCorrect);
        println("Next function preserved : " + nextPreserved);

        boolean pass =
            destructorCorrect &&
            recoveredCorrect &&
            ownershipCorrect &&
            independent &&
            branchCorrect &&
            incomingReference &&
            prologueCorrect &&
            epilogueCorrect &&
            nextPreserved;

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}