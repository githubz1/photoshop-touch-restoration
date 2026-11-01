// CorrectEleventhThumbVeneer.java
// PS Touch — Eleventh ARM Thumb veneer metadata correction
// Guarded function-body modification

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectEleventhThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — ELEVENTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
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
            println("RESULT: ABORTED — Original function missing.");
            return;
        }

        AddressSet expectedBody = new AddressSet();
        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098b82"), veneerEnd);

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(originalStart, originalEnd);

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
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");

        println("Original name correct      : " + nameCorrect);
        println("Original entry correct     : " + entryCorrect);
        println("Exact body ranges correct  : " + bodyCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean safe =
            nameCorrect
            && entryCorrect
            && bodyCorrect
            && ownershipCorrect
            && literalExcluded
            && noIndependentFunction;

        if (!safe) {
            println("");
            println("RESULT: ABORTED — Safety checks failed.");
            return;
        }

        println("");
        println("APPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function updated = fm.getFunctionAt(originalStart);

        Function veneerOwner = fm.getFunctionContaining(veneerStart);
        Function veneerEndOwner = fm.getFunctionContaining(veneerEnd);

        boolean originalBodyCorrected =
            updated != null
            && updated.getBody().hasSameAddresses(correctedBody)
            && updated.getBody().getNumAddresses() == 36;

        boolean veneerOwnershipCleared =
            veneerOwner == null
            && veneerEndOwner == null;

        boolean literalStillExcluded =
            updated != null
            && !updated.getBody().contains(literalStart)
            && !updated.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (updated == null
                ? "NONE"
                : updated.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (veneerOwner == null
                ? "NONE"
                : veneerOwner.getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Original body corrected : " + originalBodyCorrected);
        println("Veneer ownership cleared: " + veneerOwnershipCleared);
        println("Literal remains excluded: " + literalStillExcluded);

        boolean passed =
            originalBodyCorrected
            && veneerOwnershipCleared
            && literalStillExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}