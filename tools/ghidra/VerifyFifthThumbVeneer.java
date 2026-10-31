// VerifyFifthThumbVeneer.java
// PS Touch — Fifth ARM Thumb veneer preflight
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyFifthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FIFTH ARM THUMB VENEER PREFLIGHT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
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
            println("RESULT: FAIL — Original function missing.");
            println("Database changes: NONE");
            return;
        }

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098b3a"), veneerEnd);

        boolean nameCorrect =
            original.getName().equals("scanningPopover");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 265;

        boolean ownershipCorrect =
            owner == original
            && original.getBody().contains(veneerStart)
            && original.getBody().contains(veneerEnd);

        boolean literalExcluded =
            !original.getBody().contains(literalStart)
            && !original.getBody().contains(literalEnd);

        boolean noIndependentThunk =
            independent == null;

        println("");
        println("PREFLIGHT CHECKS");
        println("------------------------------------------------------------");

        println("Function name correct : " + nameCorrect);
        println("Entry point correct   : " + entryCorrect);
        println("Body ranges correct   : " + bodyCorrect);
        println("Ownership correct     : " + ownershipCorrect);
        println("Literal excluded      : " + literalExcluded);
        println("No independent thunk  : " + noIndependentThunk);

        boolean passed =
            nameCorrect
            && entryCorrect
            && bodyCorrect
            && ownershipCorrect
            && literalExcluded
            && noIndependentThunk;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}