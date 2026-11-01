// InspectObjcStoreWeakThunk.java
// PS Touch — Inspect existing Objective-C weak-reference import
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectObjcStoreWeakThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — EXISTING _objc_storeWeak IMPORT INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address entry = toAddr("01099edc");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function function = fm.getFunctionAt(entry);

        if (function == null) {
            println("");
            println("RESULT             : FAIL");
            println("Reason             : No function at expected address.");
            println("Database changes   : NONE");
            println("Executable changes : NONE");
            println("============================================================");
            return;
        }

        println("");
        println("EXISTING IMPORT");
        println("------------------------------------------------------------");

        println("Function name : " + function.getName());
        println("Entry point   : " + function.getEntryPoint());
        println("Is thunk      : " + function.isThunk());
        println("Body size     : " + function.getBody().getNumAddresses());

        Function immediate = null;
        Function resolved = null;

        if (function.isThunk()) {
            immediate = function.getThunkedFunction(false);
            resolved = function.getThunkedFunction(true);
        }

        println("");
        println("DESTINATION");
        println("------------------------------------------------------------");

        println("Immediate destination : " +
            (immediate == null ? "NONE" : immediate.getName()));

        println("Immediate address     : " +
            (immediate == null ? "NONE" : immediate.getEntryPoint()));

        println("Resolved destination  : " +
            (resolved == null ? "NONE" : resolved.getName()));

        println("Resolved address      : " +
            (resolved == null ? "NONE" : resolved.getEntryPoint()));

        println("Resolved external     : " +
            (resolved != null && resolved.isExternal()));

        boolean nameCorrect =
            function.getName().equals("_objc_storeWeak");

        boolean thunkCorrect =
            function.isThunk() &&
            function.getBody().getNumAddresses() == 8;

        boolean destinationCorrect =
            resolved != null &&
            resolved.getName().equals("_objc_storeWeak") &&
            resolved.isExternal();

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");

        println("Import name correct  : " + nameCorrect);
        println("Thunk recognized     : " + thunkCorrect);
        println("Destination correct  : " + destinationCorrect);

        boolean passed =
            nameCorrect &&
            thunkCorrect &&
            destinationCorrect;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}