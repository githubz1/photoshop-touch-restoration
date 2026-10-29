// InspectFourthReconstructedThunk.java
// PS Touch — Fourth reconstructed ARM Thumb thunk verification
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectFourthReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FOURTH RECONSTRUCTED THUNK VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address thunkStart = toAddr("01098b2c");
        Address thunkEnd = toAddr("01098b33");

        Address literalStart = toAddr("01098b34");
        Address literalEnd = toAddr("01098b37");

        Address originalStart = toAddr("00fbd6a4");
        Address originalEnd = toAddr("00fbd7cf");

        Address expectedDestination = toAddr("EXTERNAL:00000184");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function thunk = fm.getFunctionAt(thunkStart);
        Function original = fm.getFunctionAt(originalStart);

        if (thunk == null || original == null) {
            println("RESULT: FAIL — Required function missing.");
            println("Database changes: NONE");
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
            (destination == null
                ? "NONE"
                : destination.getEntryPoint()));

        println("Destination external: " +
            (destination != null && destination.isExternal()));

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
                "_objc_autoreleaseReturnValue");

        boolean literalExcluded =
            !thunk.getBody().contains(literalStart)
            && !thunk.getBody().contains(literalEnd);

        boolean originalBodyIntact =
            original.getBody().hasSameAddresses(expectedOriginalBody)
            && original.getBody().getNumAddresses() == 300;

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