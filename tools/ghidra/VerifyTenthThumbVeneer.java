// VerifyTenthThumbVeneer.java
// PS Touch — Tenth ARM Thumb veneer correction preflight

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyTenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — TENTH ARM THUMB VENEER CORRECTION PREFLIGHT");
        println("============================================================");

        Address originalStart = toAddr("00faf434");
        Address originalEnd = toAddr("00faf461");

        Address veneerStart = toAddr("01098b74");
        Address veneerEnd = toAddr("01098b7b");

        Address literalStart = toAddr("01098b7c");
        Address literalEnd = toAddr("01098b7f");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function owner = fm.getFunctionContaining(veneerStart);
        Function independent = fm.getFunctionAt(veneerStart);

        if (original == null) {
            println("RESULT: FAIL — Original function missing.");
            return;
        }

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098b76"), veneerEnd);

        boolean nameCorrect =
            original.getName().equals(
                "_42_GCDMulticastDelegate_forwardInvocation_block_invoke");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean rangesCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 53;

        boolean ownershipCorrect =
            owner == original
            && fm.getFunctionContaining(veneerEnd) == original;

        boolean literalExcluded =
            !original.getBody().contains(literalStart)
            && !original.getBody().contains(literalEnd);

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
            nameCorrect
            && entryCorrect
            && rangesCorrect
            && ownershipCorrect
            && literalExcluded
            && noIndependentFunction;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}