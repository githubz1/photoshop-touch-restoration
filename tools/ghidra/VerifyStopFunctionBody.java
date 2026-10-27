// VerifyStopFunctionBody.java
// PS Touch — Read-only function-body correction preflight.
// Compatible with Ghidra 12.1.2.
// Does not modify the database.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;

public class VerifyStopFunctionBody extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("00fb5bf0");
        Address end = toAddr("00fb5cb5");
        Address veneer = toAddr("01098b08");

        Function function =
            currentProgram.getFunctionManager().getFunctionAt(entry);

        if (function == null) {
            println("FAIL: Function not found.");
            return;
        }

        AddressSet expected = new AddressSet();
        expected.addRange(entry, end);
        expected.addRange(veneer, veneer);
        expected.addRange(toAddr("01098b0a"), toAddr("01098b0f"));

        AddressSet proposed = new AddressSet(entry, end);

        boolean bodyMatches =
            function.getBody().hasSameAddresses(expected);

        boolean entryPreserved = proposed.contains(entry);

        boolean originalPreserved =
            proposed.contains(entry, end);

        boolean veneerExcluded =
            !proposed.contains(veneer);

        boolean sizesMatch =
            function.getBody().getNumAddresses() == 205
            && proposed.getNumAddresses() == 198;

        println("========================================");
        println("PS TOUCH - FUNCTION BODY PREFLIGHT");
        println("========================================");

        println("Function          : " + function.getName());
        println("Entry             : " + function.getEntryPoint());
        println("Current size      : "
            + function.getBody().getNumAddresses());
        println("Proposed size     : "
            + proposed.getNumAddresses());

        println("");
        println("Expected body     : " + bodyMatches);
        println("Entry preserved   : " + entryPreserved);
        println("Original retained : " + originalPreserved);
        println("Veneer excluded   : " + veneerExcluded);
        println("Sizes correct     : " + sizesMatch);

        boolean passed =
            bodyMatches
            && entryPreserved
            && originalPreserved
            && veneerExcluded
            && sizesMatch;

        println("");
        println("PREFLIGHT RESULT  : "
            + (passed ? "PASS" : "FAIL"));

        println("Database changes  : NONE");
        println("========================================");
    }
}