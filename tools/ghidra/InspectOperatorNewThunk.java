// InspectOperatorNewThunk.java
// PS Touch — Inspect existing C++ operator.new import thunk.
// READ ONLY — No database modifications.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;

public class InspectOperatorNewThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("01099c84");

        Function function = currentProgram
            .getFunctionManager()
            .getFunctionAt(entry);

        println("============================================================");
        println("PS TOUCH — OPERATOR.NEW IMPORT THUNK INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        if (function == null) {
            println("FAIL: No function at " + entry);
            return;
        }

        println("Function name : " + function.getName());
        println("Entry point   : " + function.getEntryPoint());
        println("Is thunk      : " + function.isThunk());
        println("Body size     : " + function.getBody().getNumAddresses());

        println("");
        println("FUNCTION BODY RANGES");
        println("------------------------------------------------------------");

        for (AddressRange range : function.getBody()) {
            println(
                range.getMinAddress() + " - " +
                range.getMaxAddress() +
                " (" + range.getLength() + " bytes)"
            );
        }

        println("");
        println("THUNK DESTINATION");
        println("------------------------------------------------------------");

        Function immediate = function.isThunk()
            ? function.getThunkedFunction(false)
            : null;

        Function resolved = function.isThunk()
            ? function.getThunkedFunction(true)
            : null;

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

        println("");
        println("Database changes      : NONE");
        println("============================================================");
    }
}