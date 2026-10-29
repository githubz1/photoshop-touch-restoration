// InspectObjcAutoreleaseReturnValueThunk.java
// PS Touch — Objective-C autorelease return value import inspection
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectObjcAutoreleaseReturnValueThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — OBJC_AUTORELEASERETURNVALUE IMPORT INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address importThunkAddress = toAddr("01099dec");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function thunk = fm.getFunctionAt(importThunkAddress);

        if (thunk == null) {
            println("RESULT: FAIL — Import thunk not found.");
            println("Database changes: NONE");
            return;
        }

        println("");
        println("EXISTING IMPORT THUNK");
        println("------------------------------------------------------------");

        println("Function name : " + thunk.getName());
        println("Entry point   : " + thunk.getEntryPoint());
        println("Is thunk      : " + thunk.isThunk());
        println("Body size     : " + thunk.getBody().getNumAddresses());

        Function immediate = null;
        Function resolved = null;

        if (thunk.isThunk()) {
            immediate = thunk.getThunkedFunction(false);
            resolved = thunk.getThunkedFunction(true);
        }

        println("");
        println("THUNK DESTINATION");
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

        println("Resolved is external  : " +
            (resolved != null && resolved.isExternal()));

        println("");
        println("Database changes      : NONE");
        println("============================================================");
    }
}