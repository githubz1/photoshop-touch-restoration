// InspectObjcRetainAutoreleasedReturnValueThunk.java
// PS Touch — Fifth veneer destination inspection
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectObjcRetainAutoreleasedReturnValueThunk
        extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — OBJC_RETAINAUTORELEASEDRETURNVALUE IMPORT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Address importAddress = toAddr("01099ea0");

        Function imported = fm.getFunctionAt(importAddress);

        if (imported == null) {
            println("RESULT: FAIL — Import thunk not found.");
            println("Database changes: NONE");
            return;
        }

        println("");
        println("EXISTING IMPORT THUNK");
        println("------------------------------------------------------------");

        println("Function name : " + imported.getName());
        println("Entry point   : " + imported.getEntryPoint());
        println("Is thunk      : " + imported.isThunk());
        println("Body size     : " +
            imported.getBody().getNumAddresses());

        Function immediate = null;
        Function resolved = null;

        if (imported.isThunk()) {
            immediate = imported.getThunkedFunction(false);
            resolved = imported.getThunkedFunction(true);
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

        boolean passed =
            imported.isThunk()
            && imported.getName().equals(
                "_objc_retainAutoreleasedReturnValue")
            && imported.getBody().getNumAddresses() == 8
            && resolved != null
            && resolved.isExternal()
            && resolved.getName().equals(
                "_objc_retainAutoreleasedReturnValue");

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}