// InspectFifteenthReconstructedThunk.java
// PS Touch — Fifteenth reconstructed ARM Thumb thunk verification
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectFifteenthReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FIFTEENTH RECONSTRUCTED THUNK VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address thunkStart = toAddr("01098bb0");
        Address thunkEnd = toAddr("01098bb7");

        Address literalStart = toAddr("01098bb8");
        Address literalEnd = toAddr("01098bbb");

        Address originalStart = toAddr("00faf858");
        Address originalEnd = toAddr("00faf86b");

        Address expectedDestination = toAddr("EXTERNAL:00000198");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function thunk = fm.getFunctionAt(thunkStart);
        Function original = fm.getFunctionAt(originalStart);

        Function destination =
            thunk != null && thunk.isThunk()
                ? thunk.getThunkedFunction(true)
                : null;

        AddressSet expectedThunkBody = new AddressSet();
        expectedThunkBody.addRange(thunkStart, thunkEnd);

        AddressSet expectedOriginalBody = new AddressSet();
        expectedOriginalBody.addRange(originalStart, originalEnd);

        println("");
        println("RECONSTRUCTED THUNK");
        println("------------------------------------------------------------");

        println("Function name : " +
            (thunk == null ? "NONE" : thunk.getName()));

        println("Entry point   : " +
            (thunk == null ? "NONE" : thunk.getEntryPoint()));

        println("Is thunk      : " +
            (thunk != null && thunk.isThunk()));

        println("Body size     : " +
            (thunk == null
                ? "NONE"
                : thunk.getBody().getNumAddresses()));

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

        println("");
        println("ORIGINAL FUNCTION");
        println("------------------------------------------------------------");

        println("Function name : " +
            (original == null ? "NONE" : original.getName()));

        println("Entry point   : " +
            (original == null
                ? "NONE"
                : original.getEntryPoint()));

        println("Body size     : " +
            (original == null
                ? "NONE"
                : original.getBody().getNumAddresses()));

        boolean thunkRecognized =
            thunk != null &&
            thunk.isThunk() &&
            thunk.getName().equals("_objc_storeWeak");

        boolean thunkBodyCorrect =
            thunk != null &&
            thunk.getBody().hasSameAddresses(expectedThunkBody) &&
            thunk.getBody().getNumAddresses() == 8;

        boolean destinationCorrect =
            destination != null &&
            destination.getName().equals("_objc_storeWeak") &&
            destination.getEntryPoint().equals(expectedDestination) &&
            destination.isExternal();

        boolean literalExcluded =
            thunk != null &&
            !thunk.getBody().contains(literalStart) &&
            !thunk.getBody().contains(literalEnd);

        boolean originalBodyIntact =
            original != null &&
            original.getName().equals("setDelegate:") &&
            original.getBody().hasSameAddresses(expectedOriginalBody) &&
            original.getBody().getNumAddresses() == 20;

        boolean ownershipCorrect =
            thunk != null &&
            fm.getFunctionContaining(thunkStart) == thunk &&
            fm.getFunctionContaining(thunkEnd) == thunk &&
            fm.getFunctionContaining(literalStart) == null &&
            fm.getFunctionContaining(literalEnd) == null;

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");

        println("Thunk recognized     : " + thunkRecognized);
        println("Thunk body correct   : " + thunkBodyCorrect);
        println("Destination correct  : " + destinationCorrect);
        println("Literal excluded     : " + literalExcluded);
        println("Original body intact : " + originalBodyIntact);
        println("Ownership correct    : " + ownershipCorrect);

        boolean passed =
            thunkRecognized &&
            thunkBodyCorrect &&
            destinationCorrect &&
            literalExcluded &&
            originalBodyIntact &&
            ownershipCorrect;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}