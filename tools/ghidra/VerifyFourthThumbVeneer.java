// VerifyFourthThumbVeneer.java
// PS Touch — Fourth ARM Thumb veneer correction preflight
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyFourthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FOURTH ARM THUMB VENEER PREFLIGHT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Address originalStart = toAddr("00fbd6a4");
        Address originalEnd = toAddr("00fbd7cf");

        Address veneerStart = toAddr("01098b2c");
        Address veneerEnd = toAddr("01098b33");

        Address literalStart = toAddr("01098b34");
        Address literalEnd = toAddr("01098b37");

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
        expectedBody.addRange(toAddr("01098b2e"), veneerEnd);

        boolean nameCorrect =
            original.getName().equals("bitStringWithData:");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 307;

        boolean ownershipCorrect =
            owner == original
            && original.getBody().contains(veneerStart)
            && original.getBody().contains(veneerEnd);

        boolean literalExcluded =
            !original.getBody().contains(literalStart)
            && !original.getBody().contains(literalEnd);

        boolean noIndependentFunction =
            independent == null;

        println("");
        println("PREFLIGHT CHECKS");
        println("------------------------------------------------------------");

        println("Function name correct : " + nameCorrect);
        println("Entry point correct   : " + entryCorrect);
        println("Body ranges correct   : " + bodyCorrect);
        println("Ownership correct     : " + ownershipCorrect);
        println("Literal excluded      : " + literalExcluded);
        println("No independent thunk  : " + noIndependentFunction);

        boolean passed =
            nameCorrect
            && entryCorrect
            && bodyCorrect
            && ownershipCorrect
            && literalExcluded
            && noIndependentFunction;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}