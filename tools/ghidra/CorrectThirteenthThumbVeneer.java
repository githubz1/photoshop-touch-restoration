// CorrectThirteenthThumbVeneer.java
// PS Touch — Thirteenth ARM Thumb veneer metadata correction
// GUARDED FUNCTION-BODY MODIFICATION
// Modifies Ghidra function metadata only, never executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectThirteenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — THIRTEENTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
        println("============================================================");

        Address originalStart = toAddr("00f91318");
        Address originalEnd = toAddr("00f91355");

        Address veneerStart = toAddr("01098b98");
        Address veneerSecondStart = toAddr("01098b9a");
        Address veneerEnd = toAddr("01098b9f");

        Address literalStart = toAddr("01098ba0");
        Address literalEnd = toAddr("01098ba3");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function owner = fm.getFunctionContaining(veneerStart);
        Function independent = fm.getFunctionAt(veneerStart);

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(veneerSecondStart, veneerEnd);

        boolean nameCorrect =
            original != null &&
            original.getName().equals("performBlock:");

        boolean entryCorrect =
            original != null &&
            original.getEntryPoint().equals(originalStart);

        boolean rangesCorrect =
            original != null &&
            original.getBody().hasSameAddresses(expectedBody) &&
            original.getBody().getNumAddresses() == 69;

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
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");

        println("Original name correct      : " + nameCorrect);
        println("Original entry correct     : " + entryCorrect);
        println("Exact body ranges correct  : " + rangesCorrect);
        println("Veneer ownership correct   : " + ownershipCorrect);
        println("Literal excluded           : " + literalExcluded);
        println("No independent function    : " + noIndependentFunction);

        boolean safe =
            nameCorrect &&
            entryCorrect &&
            rangesCorrect &&
            ownershipCorrect &&
            literalExcluded &&
            noIndependentFunction;

        if (!safe) {
            println("");
            println("RESULT             : ABORTED");
            println("Reason             : Safety preflight failed");
            println("Database changes   : NONE");
            println("Executable changes : NONE");
            println("============================================================");
            return;
        }

        println("");
        println("APPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(originalStart, originalEnd);

        original.setBody(correctedBody);

        Function corrected = fm.getFunctionAt(originalStart);

        Function remainingOwnerStart =
            fm.getFunctionContaining(veneerStart);

        Function remainingOwnerEnd =
            fm.getFunctionContaining(veneerEnd);

        boolean originalBodyCorrect =
            corrected != null &&
            corrected.getBody().hasSameAddresses(correctedBody) &&
            corrected.getBody().getNumAddresses() == 62;

        boolean veneerOwnershipCleared =
            remainingOwnerStart == null &&
            remainingOwnerEnd == null;

        boolean literalStillExcluded =
            corrected != null &&
            !corrected.getBody().contains(literalStart) &&
            !corrected.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (corrected == null
                ? "NONE"
                : corrected.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwnerStart == null
                ? "NONE"
                : remainingOwnerStart.getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Original body corrected : " + originalBodyCorrect);
        println("Veneer ownership cleared: " + veneerOwnershipCleared);
        println("Literal remains excluded: " + literalStillExcluded);

        boolean passed =
            originalBodyCorrect &&
            veneerOwnershipCleared &&
            literalStillExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}