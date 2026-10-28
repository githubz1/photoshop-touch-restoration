// InspectThirdReconstructedThunk.java
// PS Touch — Third reconstructed ARM Thumb thunk verification
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectThirdReconstructedThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — THIRD RECONSTRUCTED THUNK VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address thunkStart = toAddr("01098b20");
        Address thunkEnd = toAddr("01098b27");
        Address literalStart = toAddr("01098b28");

        Address originalStart = toAddr("00fb8948");
        Address originalEnd = toAddr("00fb8995");

        Address expectedDestination = toAddr("EXTERNAL:0000018f");

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

        println("Destination name    : "
            + (destination == null ? "NONE" : destination.getName()));

        println("Destination address : "
            + (destination == null
                ? "NONE"
                : destination.getEntryPoint()));

        println("Destination external: "
            + (destination != null && destination.isExternal()));

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
            && destination.getName().equals("_objc_release");

        boolean literalExcluded =
            !thunk.getBody().contains(literalStart);

        boolean originalBodyIntact =
            original.getBody().hasSameAddresses(expectedOriginalBody)
            && original.getBody().getNumAddresses() == 78;

        boolean ownershipCorrect =
            fm.getFunctionContaining(thunkStart) == thunk
            && fm.getFunctionContaining(thunkEnd) == thunk
            && !original.getBody().contains(thunkStart);

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