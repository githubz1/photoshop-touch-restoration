// InspectSecondReconstructedThunk.java
// PS Touch — Independent verification of the second ARM Thumb thunk.
// READ ONLY — No database modifications.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectSecondReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address thunkEntry = toAddr("01098b14");
        Address thunkEnd = toAddr("01098b1b");
        Address literal = toAddr("01098b1c");

        Address originalEntry = toAddr("00f80bc0");
        Address originalEnd = toAddr("00f80bd7");

        Address expectedDestination =
            currentProgram.getAddressFactory()
                .getAddress("EXTERNAL:00000166");

        FunctionManager manager =
            currentProgram.getFunctionManager();

        Function thunk = manager.getFunctionAt(thunkEntry);
        Function original = manager.getFunctionAt(originalEntry);

        println("============================================================");
        println("PS TOUCH — SECOND RECONSTRUCTED THUNK VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        if (thunk == null || original == null ||
            expectedDestination == null) {

            println("RESULT: FAIL");
            println("Required function or address not found.");
            return;
        }

        AddressSet expectedThunkBody =
            new AddressSet(thunkEntry, thunkEnd);

        AddressSet expectedOriginalBody =
            new AddressSet(originalEntry, originalEnd);

        Function destination = thunk.isThunk()
            ? thunk.getThunkedFunction(false)
            : null;

        boolean thunkCorrect =
            thunk.isThunk() &&
            thunk.getBody().hasSameAddresses(expectedThunkBody);

        boolean destinationCorrect =
            destination != null &&
            destination.getEntryPoint().equals(expectedDestination) &&
            destination.isExternal();

        boolean literalExcluded =
            !thunk.getBody().contains(literal);

        boolean originalCorrect =
            original.getBody().hasSameAddresses(expectedOriginalBody);

        boolean ownershipCorrect =
            manager.getFunctionContaining(thunkEntry) == thunk;

        println("");
        println("RECONSTRUCTED THUNK");
        println("------------------------------------------------------------");

        println("Function name       : " + thunk.getName());
        println("Entry point         : " + thunk.getEntryPoint());
        println("Is thunk            : " + thunk.isThunk());
        println("Body size           : " +
            thunk.getBody().getNumAddresses());

        println("");
        println("DESTINATION");
        println("------------------------------------------------------------");

        println("Destination name    : " +
            (destination == null ? "NONE" : destination.getName()));

        println("Destination address : " +
            (destination == null ? "NONE" :
                destination.getEntryPoint()));

        println("Destination external: " +
            (destination != null && destination.isExternal()));

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");

        println("Thunk body correct  : " + thunkCorrect);
        println("Destination correct : " + destinationCorrect);
        println("Literal excluded    : " + literalExcluded);
        println("Original body intact: " + originalCorrect);
        println("Ownership correct   : " + ownershipCorrect);

        boolean passed =
            thunkCorrect &&
            destinationCorrect &&
            literalExcluded &&
            originalCorrect &&
            ownershipCorrect;

        println("");
        println("RESULT             : " +
            (passed ? "PASS" : "FAIL"));

        println("Database changes   : NONE");
        println("============================================================");
    }
}