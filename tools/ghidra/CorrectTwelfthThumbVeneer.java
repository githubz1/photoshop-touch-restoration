// CorrectTwelfthThumbVeneer.java
// PS Touch — Twelfth ARM Thumb veneer metadata correction
// GUARDED FUNCTION-BODY MODIFICATION
// Does not modify executable instructions or bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectTwelfthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — TWELFTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
        println("============================================================");

        Address originalStart = toAddr("00f91300");
        Address originalEnd = toAddr("00f91317");

        Address veneerStart = toAddr("01098b8c");
        Address veneerEnd = toAddr("01098b93");

        Address literalStart = toAddr("01098b94");
        Address literalEnd = toAddr("01098b97");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098b8e"), veneerEnd);

        boolean nameCorrect =
            original != null &&
            original.getName().equals("unmarkSocketQueueTargetQueue:");

        boolean entryCorrect =
            original != null &&
            original.getEntryPoint().equals(originalStart);

        boolean rangesCorrect =
            original != null &&
            original.getBody().hasSameAddresses(expectedBody) &&
            original.getBody().getNumAddresses() == 31;

        boolean ownershipCorrect =
            owner != null &&
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
            println("RESULT: ABORTED — Safety checks failed.");
            println("Database changes   : NONE");
            println("Executable changes : NONE");
            return;
        }

        AddressSet correctedBody = new AddressSet();
        correctedBody.addRange(originalStart, originalEnd);

        println("");
        println("APPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function corrected = fm.getFunctionAt(originalStart);
        Function remainingOwner = fm.getFunctionContaining(veneerStart);

        boolean originalBodyCorrected =
            corrected != null &&
            corrected.getBody().hasSameAddresses(correctedBody) &&
            corrected.getBody().getNumAddresses() == 24;

        boolean ownershipCleared =
            remainingOwner == null &&
            fm.getFunctionContaining(veneerEnd) == null;

        boolean literalStillExcluded =
            corrected != null &&
            !corrected.getBody().contains(literalStart) &&
            !corrected.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (corrected == null
                ? "NONE"
                : corrected.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwner == null
                ? "NONE"
                : remainingOwner.getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Original body corrected : " + originalBodyCorrected);
        println("Veneer ownership cleared: " + ownershipCleared);
        println("Literal remains excluded: " + literalStillExcluded);

        boolean passed =
            originalBodyCorrected &&
            ownershipCleared &&
            literalStillExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}