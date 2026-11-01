// InspectObjcDestroyWeakThunk.java
// PS Touch — Existing _objc_destroyWeak import inspection
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.address.Address;

public class InspectObjcDestroyWeakThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — EXISTING _objc_destroyWeak IMPORT INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function existing = null;

        for (Function function : fm.getFunctions(true)) {

            if (function.getName().equals("_objc_destroyWeak") &&
                function.isThunk() &&
                !function.getEntryPoint().isExternalAddress() &&
                !function.getEntryPoint().equals(toAddr("01098bbc"))) {

                existing = function;
                break;
            }
        }

        Function immediate =
            existing == null ? null : existing.getThunkedFunction(false);

        Function resolved =
            existing == null ? null : existing.getThunkedFunction(true);

        println("");
        println("EXISTING IMPORT");
        println("------------------------------------------------------------");

        println("Function name : " +
            (existing == null ? "NONE" : existing.getName()));

        println("Entry point   : " +
            (existing == null ? "NONE" : existing.getEntryPoint()));

        println("Is thunk      : " +
            (existing != null && existing.isThunk()));

        println("Body size     : " +
            (existing == null
                ? "NONE"
                : existing.getBody().getNumAddresses()));

        println("");
        println("DESTINATION");
        println("------------------------------------------------------------");

        println("Immediate destination : " +
            (immediate == null ? "NONE" : immediate.getName()));

        println("Immediate address     : " +
            (immediate == null
                ? "NONE"
                : immediate.getEntryPoint()));

        println("Resolved destination  : " +
            (resolved == null ? "NONE" : resolved.getName()));

        println("Resolved address      : " +
            (resolved == null
                ? "NONE"
                : resolved.getEntryPoint()));

        println("Resolved external     : " +
            (resolved != null && resolved.isExternal()));

        boolean nameCorrect =
            existing != null &&
            existing.getName().equals("_objc_destroyWeak");

        boolean thunkRecognized =
            existing != null &&
            existing.isThunk();

        boolean destinationCorrect =
            resolved != null &&
            resolved.getName().equals("_objc_destroyWeak") &&
            resolved.isExternal();

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");

        println("Import name correct  : " + nameCorrect);
        println("Thunk recognized     : " + thunkRecognized);
        println("Destination correct  : " + destinationCorrect);

        boolean passed =
            nameCorrect &&
            thunkRecognized &&
            destinationCorrect;

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}