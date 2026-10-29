// InspectFourthThumbVeneer.java
// PS Touch — Fourth ARM Thumb veneer ownership inspection
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectFourthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — FOURTH ARM THUMB VENEER INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        FunctionManager fm = currentProgram.getFunctionManager();

        Address originalStart = toAddr("00fbd6a4");

        Address veneerStart = toAddr("01098b2c");
        Address veneerEnd = toAddr("01098b33");

        Address literalStart = toAddr("01098b34");
        Address literalEnd = toAddr("01098b37");

        Function original = fm.getFunctionAt(originalStart);
        Function veneer = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        println("");
        println("ORIGINAL FUNCTION");
        println("------------------------------------------------------------");

        if (original == null) {
            println("Original function: NONE");
        }
        else {
            println("Function name : " + original.getName());
            println("Entry point   : " + original.getEntryPoint());
            println("Body size     : " +
                original.getBody().getNumAddresses());

            println("");
            println("FUNCTION BODY RANGES");
            println("------------------------------------------------------------");

            int index = 1;

            for (AddressRange range : original.getBody()) {

                println(index + ". " +
                    range.getMinAddress() + " - " +
                    range.getMaxAddress() +
                    " (" + range.getLength() + " bytes)");

                index++;
            }

            println("");
            println("VENEER OWNERSHIP");
            println("------------------------------------------------------------");

            println("Original contains start   : " +
                original.getBody().contains(veneerStart));

            println("Original contains end     : " +
                original.getBody().contains(veneerEnd));

            println("Original contains literal : " +
                original.getBody().contains(literalStart));

            println("Original contains literal end : " +
                original.getBody().contains(literalEnd));
        }

        println("");
        println("FOURTH VENEER");
        println("------------------------------------------------------------");

        println("Veneer entry       : " + veneerStart);
        println("Instruction end    : " + veneerEnd);

        println("Displacement start : " + literalStart);
        println("Displacement end   : " + literalEnd);

        println("");
        println("Independent function : " +
            (veneer == null ? "NONE" : veneer.getName()));

        println("Owning function      : " +
            (owner == null ? "NONE" : owner.getName()));

        println("Owner entry point    : " +
            (owner == null ? "NONE" : owner.getEntryPoint()));

        println("");
        println("OWNERSHIP CONSISTENCY");
        println("------------------------------------------------------------");

        boolean originalOwnsVeneer =
            original != null &&
            original.getBody().contains(veneerStart);

        boolean ownerMatchesOriginal =
            original != null &&
            owner == original;

        println("Original owns veneer : " + originalOwnsVeneer);
        println("Owner matches original : " + ownerMatchesOriginal);

        println("");
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}