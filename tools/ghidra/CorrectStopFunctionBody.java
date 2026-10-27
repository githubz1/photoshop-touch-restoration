// CorrectStopFunctionBody.java
// PS Touch — Controlled correction of one Ghidra function body.
// Modifies function metadata only; does not patch executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;

public class CorrectStopFunctionBody extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("00fb5bf0");
        Address end = toAddr("00fb5cb5");

        Function function =
            currentProgram.getFunctionManager().getFunctionAt(entry);

        if (function == null) {
            println("ABORT: Function not found.");
            return;
        }

        AddressSet expected = new AddressSet();
        expected.addRange(entry, end);
        expected.addRange(toAddr("01098b08"), toAddr("01098b08"));
        expected.addRange(toAddr("01098b0a"), toAddr("01098b0f"));

        if (!function.getBody().hasSameAddresses(expected)) {
            println("ABORT: Function body differs from verified baseline.");
            return;
        }

        if (!function.getName().equals("stop")) {
            println("ABORT: Unexpected function name.");
            return;
        }

        AddressSet corrected = new AddressSet(entry, end);

        println("PS TOUCH — CONTROLLED FUNCTION-BODY CORRECTION");
        println("Function: " + function.getName());
        println("Original body: " + function.getBody().getNumAddresses());
        println("Proposed body: " + corrected.getNumAddresses());

        function.setBody(corrected);

        boolean verified =
            function.getBody().hasSameAddresses(corrected);

        println("Correction verified: " + verified);
        println("Veneer still owned by stop: "
            + function.getBody().contains(toAddr("01098b08")));

        if (!verified) {
            throw new Exception(
                "Post-correction verification failed. Undo the operation."
            );
        }

        println("RESULT: SUCCESS");
        println("Removed 7 incorrectly associated bytes.");
        println("Executable bytes modified: NONE");
    }
}