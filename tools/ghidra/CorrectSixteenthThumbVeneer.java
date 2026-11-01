// CorrectSixteenthThumbVeneer.java
// PS Touch — Sixteenth ARM Thumb veneer metadata correction
// GUARDED FUNCTION-BODY MODIFICATION
// Does not modify executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectSixteenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — SIXTEENTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
        println("============================================================");

        Address originalStart = toAddr("00faf87c");
        Address originalEnd = toAddr("00faf88d");

        Address veneerStart = toAddr("01098bbc");
        Address veneerEnd = toAddr("01098bc3");

        Address literalStart = toAddr("01098bc4");
        Address literalEnd = toAddr("01098bc7");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function owner = fm.getFunctionContaining(veneerStart);
        Function independent = fm.getFunctionAt(veneerStart);

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(toAddr("01098bbe"), veneerEnd);

        boolean nameCorrect =
            original != null &&
            original.getName().equals(".cxx_destruct");

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
            println("Reason             : Safety preflight failed.");
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

        Function correctedOriginal = fm.getFunctionAt(originalStart);

        boolean originalBodyCorrected =
            correctedOriginal != null &&
            correctedOriginal.getName().equals(".cxx_destruct") &&
            correctedOriginal.getBody().hasSameAddresses(correctedBody) &&
            correctedOriginal.getBody().getNumAddresses() == 18;

        boolean veneerOwnershipCleared =
            fm.getFunctionContaining(veneerStart) == null &&
            fm.getFunctionContaining(veneerEnd) == null;

        boolean literalRemainsExcluded =
            correctedOriginal != null &&
            !correctedOriginal.getBody().contains(literalStart) &&
            !correctedOriginal.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (correctedOriginal == null
                ? "NONE"
                : correctedOriginal.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (fm.getFunctionContaining(veneerStart) == null
                ? "NONE"
                : fm.getFunctionContaining(veneerStart).getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Original body corrected : " + originalBodyCorrected);
        println("Veneer ownership cleared: " + veneerOwnershipCleared);
        println("Literal remains excluded: " + literalRemainsExcluded);

        boolean passed =
            originalBodyCorrected &&
            veneerOwnershipCleared &&
            literalRemainsExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}