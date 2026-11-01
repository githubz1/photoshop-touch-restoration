// VerifyFourteenthThumbVeneer.java
// PS Touch — Fourteenth ARM Thumb veneer correction preflight
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyFourteenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FOURTEENTH ARM THUMB VENEER CORRECTION PREFLIGHT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address originalStart = toAddr("00fb744c");
        Address originalEnd = toAddr("00fb745d");

        Address veneerStart = toAddr("01098ba4");
        Address veneerEnd = toAddr("01098bab");

        Address literalStart = toAddr("01098bac");
        Address literalEnd = toAddr("01098baf");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function owner = fm.getFunctionContaining(veneerStart);
        Function independent = fm.getFunctionAt(veneerStart);

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098ba6"), veneerEnd);

        boolean nameCorrect =
            original != null &&
            original.getName().equals("data");

        boolean entryCorrect =
            original != null &&
            original.getEntryPoint().equals(originalStart);

        boolean rangesCorrect =
            original != null &&
            original.getBody().hasSameAddresses(expectedBody) &&
            original.getBody().getNumAddresses() == 25;

        boolean ownershipCorrect =
            original != null &&
            owner == original &&
            fm.getFunctionContaining(veneerEnd) == original;

        boolean literalExcluded =
            original != null &&
            !original.getBody().contains(literalStart) &&
            !original.getBody().contains(literalEnd);

        boolean noIndependentFunction =
            independent == null;

        println("");
        println("PREFLIGHT CHECKS");
        println("------------------------------------------------------------");

        println("Original name correct      : " + nameCorrect);
        println("Original entry correct     : " + entryCorrect);
        println("Exact body ranges correct  : " + rangesCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean passed =
            nameCorrect &&
            entryCorrect &&
            rangesCorrect &&
            ownershipCorrect &&
            literalExcluded &&
            noIndependentFunction;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}