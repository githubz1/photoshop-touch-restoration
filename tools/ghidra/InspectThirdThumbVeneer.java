// InspectThirdThumbVeneer.java
// PS Touch — Third ARM Thumb veneer inspection
// READ ONLY — No database or executable modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class InspectThirdThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — THIRD THUMB VENEER INSPECTION (READ ONLY)");
        println("============================================================");

        Address ownerEntry = toAddr("00fb8948");
        Address originalEnd = toAddr("00fb8995");

        Address veneerStart = toAddr("01098b20");
        Address veneerEnd = toAddr("01098b27");
        Address literalStart = toAddr("01098b28");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function owner = fm.getFunctionAt(ownerEntry);

        if (owner == null) {
            println("ERROR: Original function not found.");
            println("Database changes: NONE");
            return;
        }

        println("Function name : " + owner.getName());
        println("Entry point   : " + owner.getEntryPoint());
        println("Body size     : " + owner.getBody().getNumAddresses());

        println("");
        println("FUNCTION BODY RANGES");
        println("------------------------------------------------------------");

        int index = 1;

        for (AddressRange range : owner.getBody()) {

            println(
                index + ". "
                + range.getMinAddress()
                + " - "
                + range.getMaxAddress()
                + " ("
                + range.getLength()
                + " bytes)"
            );

            index++;
        }

        println("");
        println("EXPECTED ORIGINAL BODY");
        println("------------------------------------------------------------");

        println("Start         : " + ownerEntry);
        println("End           : " + originalEnd);
        println("Expected size : 78 bytes");

        println("");
        println("VENEER OWNERSHIP");
        println("------------------------------------------------------------");

        Function containing = fm.getFunctionContaining(veneerStart);
        Function independent = fm.getFunctionAt(veneerStart);

        println("Veneer start       : " + veneerStart);
        println("Veneer end         : " + veneerEnd);
        println("Literal start      : " + literalStart);

        println("Owner contains start   : "
            + owner.getBody().contains(veneerStart));

        println("Owner contains end     : "
            + owner.getBody().contains(veneerEnd));

        println("Owner contains literal : "
            + owner.getBody().contains(literalStart));

        println("Owning function    : "
            + (containing == null ? "NONE" : containing.getName()));

        println("Owner entry point  : "
            + (containing == null
                ? "NONE"
                : containing.getEntryPoint()));

        println("Independent function: "
            + (independent == null
                ? "NONE"
                : independent.getName()));

        println("");
        println("Database changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}