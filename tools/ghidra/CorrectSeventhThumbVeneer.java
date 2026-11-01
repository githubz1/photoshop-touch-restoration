// CorrectSeventhThumbVeneer.java
// PS Touch — Seventh ARM Thumb veneer metadata correction
// GUARDED DATABASE MODIFICATION
// No executable bytes are modified.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectSeventhThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — SEVENTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
        println("============================================================");

        Address originalStart = toAddr("00fb68d4");
        Address originalEnd = toAddr("00fb68e9");

        Address veneerStart = toAddr("01098b50");
        Address veneerEnd = toAddr("01098b57");

        Address literalStart = toAddr("01098b58");
        Address literalEnd = toAddr("01098b5b");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function original = fm.getFunctionAt(originalStart);
        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        if (original == null) {
            println("RESULT: ABORT — Original function missing.");
            println("Database changes   : NONE");
            println("Executable changes : NONE");
            return;
        }

        AddressSet expectedBody = new AddressSet();

        expectedBody.addRange(originalStart, originalEnd);
        expectedBody.addRange(veneerStart, veneerStart);
        expectedBody.addRange(veneerStart.add(2), veneerEnd);

        boolean nameCorrect =
            original.getName().equals("___copy_helper_block_156");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean bodyCorrect =
            original.getBody().hasSameAddresses(expectedBody)
            && original.getBody().getNumAddresses() == 29;

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
            && updated.getBody().getNumAddresses() == 22;

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