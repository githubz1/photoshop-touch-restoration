// InspectEleventhReconstructedThunk.java
// PS Touch — Eleventh reconstructed ARM Thumb thunk verification

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectEleventhReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — ELEVENTH RECONSTRUCTED THUNK VERIFICATION");
        println("============================================================");

        Address thunkStart = toAddr("01098b80");
        Address thunkEnd = toAddr("01098b87");

        Address literalStart = toAddr("01098b88");
        Address literalEnd = toAddr("01098b8b");

        Address originalStart = toAddr("00f87aec");
        Address originalEnd = toAddr("00f87b0f");

        Address expectedDestination = toAddr("EXTERNAL:0000004c");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function thunk = fm.getFunctionAt(thunkStart);
        Function original = fm.getFunctionAt(originalStart);

        if (thunk == null || original == null) {
            println("RESULT: FAIL — Required function missing.");
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
            && destination.getName().equals("_close");

        boolean literalExcluded =
            !thunk.getBody().contains(literalStart)
            && !thunk.getBody().contains(literalEnd);

        boolean originalBodyIntact =
            original.getName().equals(
                "_57_GCDAsyncUdpSocket_setupSendAndReceiveSourcesForSocket6_block_invoke307")
            && original.getBody().hasSameAddresses(expectedOriginalBody)
            && original.getBody().getNumAddresses() == 36;

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
        println("============================================================");
    }
}