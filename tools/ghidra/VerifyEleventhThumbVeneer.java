// VerifyEleventhThumbVeneer.java
// PS Touch — Eleventh ARM Thumb veneer correction preflight

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyEleventhThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — ELEVENTH ARM THUMB VENEER CORRECTION PREFLIGHT");
        println("============================================================");

        Address originalStart = toAddr("00f87aec");
        Address originalEnd = toAddr("00f87b0f");

        Address veneerStart = toAddr("01098b80");
        Address veneerEnd = toAddr("01098b87");

        Address literalStart = toAddr("01098b88");
        Address literalEnd = toAddr("01098b8b");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        if (original == null) {
            println("RESULT: FAIL — Original function missing.");
            return;
        }

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098b82"), veneerEnd);

        boolean nameCorrect =
            original.getName().equals(
                "_57_GCDAsyncUdpSocket_setupSendAndReceiveSourcesForSocket6_block_invoke307");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 43;

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
        println("Exact body ranges correct  : " + bodyCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean passed =
            nameCorrect
            && entryCorrect
            && bodyCorrect
            && ownershipCorrect
            && literalExcluded
            && noIndependentFunction;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}