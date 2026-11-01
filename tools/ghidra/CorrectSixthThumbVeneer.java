// CorrectSixthThumbVeneer.java
// PS Touch — Sixth ARM Thumb veneer function-body correction
// GUARDED DATABASE METADATA MODIFICATION
// Original executable bytes are never modified.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectSixthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — SIXTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
        println("============================================================");

        Address originalStart = toAddr("00fa9200");
        Address originalEnd = toAddr("00fa921d");

        Address veneerStart = toAddr("01098b44");
        Address veneerEnd = toAddr("01098b4b");

        Address literalStart = toAddr("01098b4c");
        Address literalEnd = toAddr("01098b4f");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        if (original == null) {
            println("RESULT: ABORT — Original function missing.");
            return;
        }

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(veneerStart.add(2), veneerEnd);

        boolean nameCorrect =
            original.getName().equals("___copy_helper_block_429");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 37;

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
            println("RESULT: ABORT — Database differs from verified preflight.");
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

        Function updated = fm.getFunctionAt(originalStart);
        Function remainingOwner = fm.getFunctionContaining(veneerStart);

        boolean bodyCorrected =
            updated != null
            && updated.getBody().hasSameAddresses(correctedBody)
            && updated.getBody().getNumAddresses() == 30;

        boolean veneerReleased =
            remainingOwner == null
            && fm.getFunctionContaining(veneerEnd) == null;

        boolean literalStillExcluded =
            updated != null
            && !updated.getBody().contains(literalStart)
            && !updated.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (updated == null
                ? "NONE"
                : updated.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwner == null
                ? "NONE"
                : remainingOwner.getName()));

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Original body corrected : " + bodyCorrected);
        println("Veneer ownership cleared: " + veneerReleased);
        println("Literal remains excluded: " + literalStillExcluded);

        boolean passed =
            bodyCorrected
            && veneerReleased
            && literalStillExcluded;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}