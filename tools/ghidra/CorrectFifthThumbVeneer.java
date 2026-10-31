// CorrectFifthThumbVeneer.java
// PS Touch — Fifth ARM Thumb veneer metadata correction
// Modifies function metadata only; does not patch executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectFifthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FIFTH ARM THUMB VENEER CORRECTION");
        println("METADATA ONLY — NO EXECUTABLE BYTE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Address originalStart = toAddr("00f94834");
        Address originalEnd = toAddr("00f94935");

        Address veneerStart = toAddr("01098b38");
        Address veneerEnd = toAddr("01098b3f");

        Address literalStart = toAddr("01098b40");
        Address literalEnd = toAddr("01098b43");

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        if (original == null) {
            println("ABORT: Original function missing.");
            println("No changes performed.");
            return;
        }

        AddressSet expectedOldBody = new AddressSet();

        expectedOldBody.addRange(originalStart, originalEnd);
        expectedOldBody.addRange(veneerStart, veneerStart);
        expectedOldBody.addRange(toAddr("01098b3a"), veneerEnd);

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(originalStart, originalEnd);

        boolean nameCorrect =
            original.getName().equals("scanningPopover");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean oldBodyCorrect =
            original.getBody().hasSameAddresses(expectedOldBody)
            && original.getBody().getNumAddresses() == 265;

        boolean ownershipCorrect =
            owner == original
            && original.getBody().contains(veneerStart)
            && original.getBody().contains(veneerEnd);

        boolean noIndependentThunk =
            independent == null;

        boolean literalExcluded =
            !original.getBody().contains(literalStart)
            && !original.getBody().contains(literalEnd);

        println("");
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");

        println("Function name correct : " + nameCorrect);
        println("Entry point correct   : " + entryCorrect);
        println("Old body correct      : " + oldBodyCorrect);
        println("Ownership correct     : " + ownershipCorrect);
        println("No independent thunk  : " + noIndependentThunk);
        println("Literal excluded      : " + literalExcluded);

        boolean safe =
            nameCorrect
            && entryCorrect
            && oldBodyCorrect
            && ownershipCorrect
            && noIndependentThunk
            && literalExcluded;

        if (!safe) {
            println("");
            println("ABORT: Database differs from verified preflight.");
            println("No changes performed.");
            return;
        }

        println("");
        println("APPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function updated = fm.getFunctionAt(originalStart);
        Function remainingOwner =
            fm.getFunctionContaining(veneerStart);

        boolean corrected =
            updated != null
            && updated.getBody().hasSameAddresses(correctedBody)
            && updated.getBody().getNumAddresses() == 258
            && remainingOwner == null
            && fm.getFunctionContaining(veneerEnd) == null;

        println("Corrected body size : " +
            (updated == null
                ? "NONE"
                : updated.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwner == null
                ? "NONE"
                : remainingOwner.getName()));

        println("");
        println("RESULT             : " +
            (corrected ? "PASS" : "FAIL"));

        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}