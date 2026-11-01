// CorrectNinthThumbVeneer.java
// PS Touch — Ninth ARM Thumb veneer metadata correction

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectNinthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — NINTH ARM THUMB VENEER METADATA CORRECTION");
        println("GUARDED FUNCTION-BODY MODIFICATION");
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
        expectedBody.addRange(veneerStart.add(2), veneerEnd);

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
            owner == original &&
            original != null &&
            original.getBody().contains(veneerStart) &&
            original.getBody().contains(veneerEnd);

        boolean literalExcluded =
            original != null &&
            !original.getBody().contains(literalStart) &&
            !original.getBody().contains(literalEnd);

        boolean noIndependentFunction = independent == null;

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
            nameCorrect &&
            entryCorrect &&
            bodyCorrect &&
            ownershipCorrect &&
            literalExcluded &&
            noIndependentFunction;

        if (!safe) {
            println("");
            println("RESULT: ABORTED — Safety checks failed.");
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

        boolean originalBodyCorrected =
            updated != null &&
            updated.getBody().hasSameAddresses(correctedBody) &&
            updated.getBody().getNumAddresses() == 44;

        boolean veneerOwnershipCleared =
            remainingOwner == null &&
            fm.getFunctionContaining(veneerEnd) == null;

        boolean literalRemainsExcluded =
            updated != null &&
            !updated.getBody().contains(literalStart) &&
            !updated.getBody().contains(literalEnd);

        println("Corrected body size : " +
            (updated == null ? "NONE" :
                updated.getBody().getNumAddresses()));

        println("Veneer owner        : " +
            (remainingOwner == null ? "NONE" :
                remainingOwner.getName()));

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
        println("============================================================");
    }
}