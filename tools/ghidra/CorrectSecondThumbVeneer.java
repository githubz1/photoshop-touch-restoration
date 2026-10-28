// CorrectSecondThumbVeneer.java
// PS Touch — Controlled correction of the second ARM Thumb veneer.
// Modifies function metadata only; does not patch executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;

public class CorrectSecondThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("00f80bc0");
        Address end = toAddr("00f80bd7");
        Address veneer = toAddr("01098b14");

        Function function =
            currentProgram.getFunctionManager().getFunctionAt(entry);

        if (function == null) {
            println("ABORT: Function not found.");
            return;
        }

        if (!function.getName().equals("FUN_00f80bc0")) {
            println("ABORT: Unexpected function name.");
            return;
        }

        AddressSet expected = new AddressSet();
        expected.addRange(entry, end);
        expected.addRange(veneer, veneer);
        expected.addRange(toAddr("01098b16"), toAddr("01098b1b"));

        if (!function.getBody().hasSameAddresses(expected)) {
            println("ABORT: Function body differs from verified baseline.");
            return;
        }

        if (currentProgram.getFunctionManager()
                .getFunctionAt(veneer) != null) {
            println("ABORT: Veneer already has an independent function.");
            return;
        }

        AddressSet corrected = new AddressSet(entry, end);

        println("============================================================");
        println("PS TOUCH — SECOND VENEER FUNCTION-BODY CORRECTION");
        println("============================================================");

        println("Function      : " + function.getName());
        println("Original body : " + function.getBody().getNumAddresses());
        println("Proposed body : " + corrected.getNumAddresses());

        function.setBody(corrected);

        boolean verified =
            function.getBody().hasSameAddresses(corrected);

        boolean veneerDetached =
            !function.getBody().contains(veneer);

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Body correct    : " + verified);
        println("Veneer detached : " + veneerDetached);

        if (!verified || !veneerDetached) {
            throw new Exception(
                "Post-correction verification failed. Undo the operation."
            );
        }

        println("");
        println("RESULT: SUCCESS");
        println("Removed 7 incorrectly associated veneer bytes.");
        println("Executable bytes modified: NONE");
        println("============================================================");
    }
}