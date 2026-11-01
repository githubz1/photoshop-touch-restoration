// CorrectTenthThumbVeneer.java
// PS Touch — Tenth ARM Thumb veneer metadata correction

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectTenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — TENTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
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
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");
        println("Original name correct      : " + nameCorrect);
        println("Original entry correct     : " + entryCorrect);
        println("Exact body ranges correct  : " + rangesCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean safe =
            nameCorrect
            && entryCorrect
            && rangesCorrect
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

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(originalStart, originalEnd);

        original.setBody(correctedBody);

        Function correctedOriginal = fm.getFunctionAt(originalStart);
        Function remainingOwner = fm.getFunctionContaining(veneerStart);

        boolean originalBodyCorrect =
            correctedOriginal != null
            && correctedOriginal.getBody().hasSameAddresses(correctedBody)
            && correctedOriginal.getBody().getNumAddresses() == 46;

        boolean veneerOwnershipCleared =
            remainingOwner == null
            && fm.getFunctionContaining(veneerEnd) == null;

        boolean literalStillExcluded =
            correctedOriginal != null
            && !correctedOriginal.getBody().contains(literalStart)
            && !correctedOriginal.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (correctedOriginal == null
                ? "NONE"
                : correctedOriginal.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwner == null ? "NONE" : remainingOwner.getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Original body corrected : " + originalBodyCorrect);
        println("Veneer ownership cleared: " + veneerOwnershipCleared);
        println("Literal remains excluded: " + literalStillExcluded);

        boolean passed =
            originalBodyCorrect
            && veneerOwnershipCleared
            && literalStillExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}