// InspectObjcReleaseThunk.java
// PS Touch — Inspect existing Objective-C runtime import thunk
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectObjcReleaseThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — OBJC_RELEASE IMPORT THUNK INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address importEntry = toAddr("01099e70");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function imported = fm.getFunctionAt(importEntry);

        if (imported == null) {
            println("RESULT: FAIL — Import thunk not found.");
            return;
        }

        println("Function name : " + imported.getName());
        println("Entry point   : " + imported.getEntryPoint());
        println("Is thunk      : " + imported.isThunk());
        println("Body size     : " + imported.getBody().getNumAddresses());

        println("");
        println("THUNK DESTINATION");
        println("------------------------------------------------------------");

        if (!imported.isThunk()) {
            println("RESULT: FAIL — Function is not a thunk.");
            return;
        }

        Function immediate = imported.getThunkedFunction(false);
        Function resolved = imported.getThunkedFunction(true);

        println("Immediate destination : "
            + (immediate == null ? "NONE" : immediate.getName()));

        println("Immediate address     : "
            + (immediate == null
                ? "NONE"
                : immediate.getEntryPoint()));

        println("Resolved destination  : "
            + (resolved == null ? "NONE" : resolved.getName()));

        println("Resolved address      : "
            + (resolved == null
                ? "NONE"
                : resolved.getEntryPoint()));

        println("Resolved is external  : "
            + (resolved != null && resolved.isExternal()));

        println("");
        println("Database changes      : NONE");
        println("============================================================");
    }
}