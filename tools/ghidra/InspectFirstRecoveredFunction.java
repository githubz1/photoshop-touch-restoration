// Independently verify the first recovered LC_FUNCTION_STARTS function.
// @category PS Touch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class InspectFirstRecoveredFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address wrapperEntry = toAddr("00df81dc");
        Address recoveredEntry = toAddr("00df81e8");
        Address recoveredEnd = toAddr("00df8235");

        Function wrapper =
            currentProgram.getFunctionManager().getFunctionAt(wrapperEntry);

        Function recovered =
            currentProgram.getFunctionManager().getFunctionAt(recoveredEntry);

        println("============================================================");
        println("PS TOUCH — FIRST RECOVERED FUNCTION VERIFICATION");
        println("============================================================");

        boolean wrapperCorrect =
            wrapper != null &&
            wrapper.getBody().getNumAddresses() == 12 &&
            wrapper.getBody().getMinAddress().equals(wrapperEntry) &&
            wrapper.getBody().getMaxAddress().equals(toAddr("00df81e7"));

        boolean recoveredCorrect =
            recovered != null &&
            recovered.getEntryPoint().equals(recoveredEntry) &&
            recovered.getBody().getNumAddresses() == 78 &&
            recovered.getBody().getMinAddress().equals(recoveredEntry) &&
            recovered.getBody().getMaxAddress().equals(recoveredEnd);

        boolean ownershipCorrect =
            recovered != null &&
            currentProgram.getFunctionManager()
                .getFunctionContaining(recoveredEntry) == recovered &&
            currentProgram.getFunctionManager()
                .getFunctionContaining(recoveredEnd) == recovered;

        boolean separateFunctions =
            wrapper != null &&
            recovered != null &&
            !wrapper.getBody().intersects(recovered.getBody());

        Instruction prologue =
            currentProgram.getListing().getInstructionAt(recoveredEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(recoveredEntry)) {
            if (reference.getFromAddress().equals(toAddr("01098b00")) &&
                reference.getReferenceType() == RefType.COMPUTED_JUMP) {
                incomingReference = true;
            }
        }

        println("\nRECOVERED FUNCTIONS");
        println("------------------------------------------------------------");

        println("Wrapper entry   : " + wrapperEntry);
        println("Wrapper size    : " +
            (wrapper == null ? "NONE" : wrapper.getBody().getNumAddresses()));

        println("Recovered entry : " + recoveredEntry);
        println("Recovered size  : " +
            (recovered == null ? "NONE" : recovered.getBody().getNumAddresses()));

        println("\nVERIFICATION");
        println("------------------------------------------------------------");
        println("Wrapper body correct   : " + wrapperCorrect);
        println("Recovered body correct : " + recoveredCorrect);
        println("Ownership correct      : " + ownershipCorrect);
        println("Functions independent  : " + separateFunctions);
        println("Original prologue      : " + prologueCorrect);
        println("Incoming reference     : " + incomingReference);

        boolean pass =
            wrapperCorrect &&
            recoveredCorrect &&
            ownershipCorrect &&
            separateFunctions &&
            prologueCorrect &&
            incomingReference;

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}