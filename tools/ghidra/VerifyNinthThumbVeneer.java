// VerifyNinthThumbVeneer.java
// PS Touch — Ninth ARM Thumb veneer correction preflight

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyNinthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — NINTH ARM THUMB VENEER CORRECTION PREFLIGHT");
        println("============================================================");

        Address originalStart = toAddr("00f8fb34");
        Address originalEnd = toAddr("00f8fb5f");

        Address veneerStart = toAddr("01098b68");
        Address veneerEnd = toAddr("01098b6f");

        Address literalStart = toAddr("01098b70");
        Address literalEnd = toAddr("01098b73");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(
            veneerStart.add(2),
            veneerEnd
        );

        boolean nameCorrect =
            original != null &&
            original.getName().equals("___destroy_helper_block_679");

        boolean entryCorrect =
            original != null &&
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original != null &&
            original.getBody().hasSameAddresses(expectedBody) &&
            original.getBody().getNumAddresses() == 51;

        boolean ownershipCorrect =
            owner != null &&
            owner == original &&
            original.getBody().contains(veneerStart) &&
            original.getBody().contains(veneerEnd);

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
        println("Exact body ranges correct  : " + bodyCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean passed =
            nameCorrect &&
            entryCorrect &&
            bodyCorrect &&
            ownershipCorrect &&
            literalExcluded &&
            noIndependentFunction;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}