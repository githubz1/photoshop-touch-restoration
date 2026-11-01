// InspectDispatchQueueSetSpecificThunk.java
// PS Touch — dispatch_queue_set_specific import verification
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectDispatchQueueSetSpecificThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — DISPATCH_QUEUE_SET_SPECIFIC IMPORT VERIFICATION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address importAddress = toAddr("01098fd0");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function thunk = fm.getFunctionAt(importAddress);

        if (thunk == null) {
            println("RESULT: FAIL — Import thunk not found.");
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
            (immediate == null ? "NONE" : immediate.getEntryPoint()));

        println("Resolved destination  : " +
            (resolved == null ? "NONE" : resolved.getName()));

        println("Resolved address      : " +
            (resolved == null ? "NONE" : resolved.getEntryPoint()));

        println("Resolved is external  : " +
            (resolved != null && resolved.isExternal()));

        boolean passed =
            thunk.isThunk() &&
            thunk.getName().equals("_dispatch_queue_set_specific") &&
            thunk.getBody().getNumAddresses() == 8 &&
            resolved != null &&
            resolved.isExternal() &&
            resolved.getName().equals("_dispatch_queue_set_specific");

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}