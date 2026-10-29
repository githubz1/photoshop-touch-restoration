// CorrectFourthThumbVeneer.java
// PS Touch — Fourth ARM Thumb veneer function-body correction
// METADATA ONLY — Original executable bytes remain unchanged

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectFourthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FOURTH ARM THUMB VENEER CORRECTION");
        println("METADATA ONLY — NO EXECUTABLE BYTE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Address originalStart = toAddr("00fbd6a4");
        Address originalEnd = toAddr("00fbd7cf");

        Address veneerStart = toAddr("01098b2c");
        Address veneerEnd = toAddr("01098b33");

        Address splitStart = toAddr("01098b2e");

        Address literalStart = toAddr("01098b34");
        Address literalEnd = toAddr("01098b37");

        Function original = fm.getFunctionAt(originalStart);

        if (original == null) {
            println("ABORT: Original function not found.");
            return;
        }

        AddressSet expectedOldBody = new AddressSet();

        expectedOldBody.addRange(originalStart, originalEnd);
        expectedOldBody.addRange(veneerStart, veneerStart);
        expectedOldBody.addRange(splitStart, veneerEnd);

        AddressSet correctedBody = new AddressSet();

        correctedBody.addRange(originalStart, originalEnd);

        boolean nameCorrect =
            original.getName().equals("bitStringWithData:");

        boolean entryCorrect =
            original.getEntryPoint().equals(originalStart);

        boolean oldBodyCorrect =
            original.getBody().hasSameAddresses(expectedOldBody)
            && original.getBody().getNumAddresses() == 307;

        boolean ownershipCorrect =
            fm.getFunctionContaining(veneerStart) == original
            && fm.getFunctionContaining(veneerEnd) == original;

        boolean noIndependentFunction =
            fm.getFunctionAt(veneerStart) == null;

        boolean literalExcluded =
            !original.getBody().contains(literalStart)
            && !original.getBody().contains(literalEnd);

        println("");
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");

        println("Function name correct : " + nameCorrect);
        println("Entry point correct   : " + entryCorrect);
        println("Old body correct      : " + oldBodyCorrect);
        println("Ownership correct     : " + ownershipCorrect);
        println("No independent thunk  : " + noIndependentFunction);
        println("Literal excluded      : " + literalExcluded);

        boolean safe =
            nameCorrect
            && entryCorrect
            && oldBodyCorrect
            && ownershipCorrect
            && noIndependentFunction
            && literalExcluded;

        if (!safe) {
            println("");
            println("ABORT: Database differs from verified preflight.");
            println("No changes performed.");
            return;
        }

        println("");
        println("APPLYING FUNCTION-BODY CORRECTION");
        println("------------------------------------------------------------");

        original.setBody(correctedBody);

        Function updated = fm.getFunctionAt(originalStart);

        boolean corrected =
            updated != null
            && updated.getBody().hasSameAddresses(correctedBody)
            && updated.getBody().getNumAddresses() == 300;

        boolean veneerReleased =
            fm.getFunctionContaining(veneerStart) == null
            && fm.getFunctionContaining(veneerEnd) == null;

        println("Corrected body size : " +
            (updated == null
                ? "FUNCTION MISSING"
                : updated.getBody().getNumAddresses()));

        println("Body correction verified : " + corrected);
        println("Veneer ownership released: " + veneerReleased);

        println("");
        println("RESULT: " +
            (corrected && veneerReleased ? "PASS" : "FAIL"));

        println("Executable byte modifications: NONE");
        println("============================================================");
    }
}