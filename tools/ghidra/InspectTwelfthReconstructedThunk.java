// InspectTwelfthReconstructedThunk.java
// PS Touch — Twelfth reconstructed ARM Thumb thunk verification
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectTwelfthReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — TWELFTH RECONSTRUCTED THUNK VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address thunkStart = toAddr("01098b8c");
        Address thunkEnd = toAddr("01098b93");

        Address literalStart = toAddr("01098b94");
        Address literalEnd = toAddr("01098b97");

        Address originalStart = toAddr("00f91300");
        Address originalEnd = toAddr("00f91317");

        Address expectedDestination = toAddr("EXTERNAL:00000057");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function thunk = fm.getFunctionAt(thunkStart);
        Function original = fm.getFunctionAt(originalStart);

        if (thunk == null || original == null) {
            println("RESULT: FAIL — Required function missing.");
            println("Database changes   : NONE");
            println("Executable changes : NONE");
            return;
        }

        println("");
        println("RECONSTRUCTED THUNK");
        println("------------------------------------------------------------");
        println("Function name : " + thunk.getName());
        println("Entry point   : " + thunk.getEntryPoint());
        println("Is thunk      : " + thunk.isThunk());
        println("Body size     : " + thunk.getBody().getNumAddresses());

        Function destination = null;

        if (thunk.isThunk()) {
            destination = thunk.getThunkedFunction(true);
        }

        println("");
        println("DESTINATION");
        println("------------------------------------------------------------");
        println("Destination name    : " +
            (destination == null ? "NONE" : destination.getName()));
        println("Destination address : " +
            (destination == null ? "NONE" : destination.getEntryPoint()));
        println("Destination external: " +
            (destination != null && destination.isExternal()));

        println("");
        println("ORIGINAL FUNCTION");
        println("------------------------------------------------------------");
        println("Function name : " + original.getName());
        println("Entry point   : " + original.getEntryPoint());
        println("Body size     : " + original.getBody().getNumAddresses());

        AddressSet expectedThunkBody = new AddressSet();
        expectedThunkBody.addRange(thunkStart, thunkEnd);

        AddressSet expectedOriginalBody = new AddressSet();
        expectedOriginalBody.addRange(originalStart, originalEnd);

        boolean thunkBodyCorrect =
            thunk.getBody().hasSameAddresses(expectedThunkBody)
            && thunk.getBody().getNumAddresses() == 8;

        boolean destinationCorrect =
            destination != null
            && destination.isExternal()
            && destination.getEntryPoint().equals(expectedDestination)
            && destination.getName().equals(
                "_dispatch_queue_set_specific");

        boolean literalExcluded =
            !thunk.getBody().contains(literalStart)
            && !thunk.getBody().contains(literalEnd);

        boolean originalBodyIntact =
            original.getName().equals("unmarkSocketQueueTargetQueue:")
            && original.getBody().hasSameAddresses(expectedOriginalBody)
            && original.getBody().getNumAddresses() == 24;

        boolean ownershipCorrect =
            fm.getFunctionContaining(thunkStart) == thunk
            && fm.getFunctionContaining(thunkEnd) == thunk
            && !original.getBody().contains(thunkStart)
            && !original.getBody().contains(thunkEnd);

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");
        println("Thunk recognized     : " + thunk.isThunk());
        println("Thunk body correct   : " + thunkBodyCorrect);
        println("Destination correct  : " + destinationCorrect);
        println("Literal excluded     : " + literalExcluded);
        println("Original body intact : " + originalBodyIntact);
        println("Ownership correct    : " + ownershipCorrect);

        boolean passed =
            thunk.isThunk()
            && thunkBodyCorrect
            && destinationCorrect
            && literalExcluded
            && originalBodyIntact
            && ownershipCorrect;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}