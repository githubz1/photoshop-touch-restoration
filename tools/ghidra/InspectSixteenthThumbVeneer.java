// InspectSixteenthThumbVeneer.java
// PS Touch — Sixteenth ARM Thumb veneer inspection
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectSixteenthThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — SIXTEENTH ARM THUMB VENEER INSPECTION");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address veneerStart = toAddr("01098bbc");
        Address veneerEnd = toAddr("01098bc3");

        Address literalStart = toAddr("01098bc4");
        Address literalEnd = toAddr("01098bc7");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function independent = fm.getFunctionAt(veneerStart);
        Function owner = fm.getFunctionContaining(veneerStart);

        println("");
        println("SIXTEENTH VENEER");
        println("------------------------------------------------------------");

        println("Veneer entry       : " + veneerStart);
        println("Instruction end    : " + veneerEnd);
        println("Displacement start : " + literalStart);
        println("Displacement end   : " + literalEnd);

        println("");
        println("FUNCTION OWNERSHIP");
        println("------------------------------------------------------------");

        println("Independent function : " +
            (independent == null ? "NONE" : independent.getName()));

        println("Owning function      : " +
            (owner == null ? "NONE" : owner.getName()));

        println("Owner entry point    : " +
            (owner == null ? "NONE" : owner.getEntryPoint()));

        if (owner != null) {

            println("");
            println("OWNING FUNCTION");
            println("------------------------------------------------------------");

            println("Function name : " + owner.getName());
            println("Entry point   : " + owner.getEntryPoint());
            println("Body size     : " +
                owner.getBody().getNumAddresses());

            println("");
            println("FUNCTION BODY RANGES");
            println("------------------------------------------------------------");

            int index = 1;

            for (AddressRange range : owner.getBody()) {

                println(index + ". " +
                    range.getMinAddress() + " - " +
                    range.getMaxAddress() + " (" +
                    range.getLength() + " bytes)");

                index++;
            }

            println("");
            println("Owner contains start       : " +
                owner.getBody().contains(veneerStart));

            println("Owner contains end         : " +
                owner.getBody().contains(veneerEnd));

            println("Owner contains literal     : " +
                owner.getBody().contains(literalStart));

            println("Owner contains literal end : " +
                owner.getBody().contains(literalEnd));
        }

        println("");
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}