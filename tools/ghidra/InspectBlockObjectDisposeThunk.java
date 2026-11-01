// InspectBlockObjectDisposeThunk.java
// PS Touch — Block_object_dispose import verification

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectBlockObjectDisposeThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — BLOCK_OBJECT_DISPOSE IMPORT VERIFICATION");
        println("============================================================");

        Address importAddress = toAddr("01098d78");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function importThunk = fm.getFunctionAt(importAddress);

        if (importThunk == null) {
            println("RESULT: FAIL — Import thunk not found.");
            return;
        }

        println("");
        println("EXISTING IMPORT THUNK");
        println("------------------------------------------------------------");
        println("Function name : " + importThunk.getName());
        println("Entry point   : " + importThunk.getEntryPoint());
        println("Is thunk      : " + importThunk.isThunk());
        println("Body size     : " +
            importThunk.getBody().getNumAddresses());

        Function immediate = null;
        Function resolved = null;

        if (importThunk.isThunk()) {
            immediate = importThunk.getThunkedFunction(false);
            resolved = importThunk.getThunkedFunction(true);
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
            importThunk.isThunk() &&
            importThunk.getName().equals("__Block_object_dispose") &&
            importThunk.getBody().getNumAddresses() == 8 &&
            resolved != null &&
            resolved.isExternal() &&
            resolved.getName().equals("__Block_object_dispose");

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}