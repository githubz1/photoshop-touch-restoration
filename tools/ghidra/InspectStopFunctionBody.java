// InspectStopFunctionBody.java
// PS Touch — Read-only inspection of Ghidra function-body ranges.
//
// Target function: 00fb5bf0
// Suspected branch veneer: 01098b08
//
// No database modifications.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectStopFunctionBody extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FUNCTION BODY INSPECTION (READ ONLY)");
        println("============================================================");

        Address functionAddress = toAddr("00fb5bf0");
        Address veneerAddress = toAddr("01098b08");

        FunctionManager manager = currentProgram.getFunctionManager();

        Function function = manager.getFunctionAt(functionAddress);

        if (function == null) {
            println("ERROR: No function defined at " + functionAddress);
            return;
        }

        println("Function name : " + function.getName());
        println("Entry point   : " + function.getEntryPoint());
        println("Body size     : " + function.getBody().getNumAddresses());
        println("");

        println("FUNCTION BODY RANGES");
        println("------------------------------------------------------------");

        int count = 0;

        for (AddressRange range : function.getBody()) {

            count++;

            println(String.format(
                "%2d. %s - %s (%d bytes)",
                count,
                range.getMinAddress(),
                range.getMaxAddress(),
                range.getLength()
            ));
        }

        println("------------------------------------------------------------");
        println("Total ranges  : " + count);
        println("");

        boolean containsVeneer = function.getBody().contains(veneerAddress);

        println("VENEER OWNERSHIP");
        println("------------------------------------------------------------");
        println("Veneer address       : " + veneerAddress);
        println("Contained in body    : " + containsVeneer);

        Function owner = manager.getFunctionContaining(veneerAddress);

        if (owner != null) {
            println("Owning function      : " + owner.getName());
            println("Owner entry point    : " + owner.getEntryPoint());
        } else {
            println("Owning function      : NONE");
        }

        println("");
        println("Database changes     : NONE");
        println("============================================================");
    }
}