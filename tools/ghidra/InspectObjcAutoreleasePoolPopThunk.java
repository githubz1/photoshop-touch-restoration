// InspectObjcAutoreleasePoolPopThunk.java
// PS Touch — Objective-C autorelease pool pop import verification

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectObjcAutoreleasePoolPopThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — OBJC AUTORELEASE POOL POP IMPORT VERIFICATION");
        println("============================================================");

        Address thunkAddress = toAddr("01099dd4");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function thunk = fm.getFunctionAt(thunkAddress);

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
            thunk.isThunk()
            && thunk.getBody().getNumAddresses() == 8
            && resolved != null
            && resolved.isExternal()
            && resolved.getName().equals("_objc_autoreleasePoolPop");

        println("");
        println("RESULT             : " + (passed ? "PASS" : "FAIL"));
        println("============================================================");
    }
}