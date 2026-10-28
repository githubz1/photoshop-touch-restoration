import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;

public class InspectSecondThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address functionEntry = toAddr("00f80bc0");
        Address veneerStart = toAddr("01098b14");
        Address veneerEnd = toAddr("01098b1b");
        Address literal = toAddr("01098b1c");

        Function function = currentProgram
            .getFunctionManager()
            .getFunctionAt(functionEntry);

        println("============================================================");
        println("PS TOUCH — SECOND THUMB VENEER INSPECTION (READ ONLY)");
        println("============================================================");

        if (function == null) {
            println("FAIL: Original function not found.");
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

            println(
                count + ". " +
                range.getMinAddress() + " - " +
                range.getMaxAddress() +
                " (" + range.getLength() + " bytes)"
            );
        }

        println("");
        println("VENEER OWNERSHIP");
        println("------------------------------------------------------------");

        Function owner = currentProgram
            .getFunctionManager()
            .getFunctionContaining(veneerStart);

        Function independent = currentProgram
            .getFunctionManager()
            .getFunctionAt(veneerStart);

        println("Veneer start       : " + veneerStart);
        println("Veneer end         : " + veneerEnd);
        println("Contained in body  : " +
            function.getBody().contains(veneerStart, veneerEnd));

        println("Owning function    : " +
            (owner == null ? "NONE" : owner.getName()));

        println("Owner entry point  : " +
            (owner == null ? "NONE" : owner.getEntryPoint()));

        println("Independent function: " +
            (independent == null ? "NONE" : independent.getName()));

        println("Literal in body    : " +
            function.getBody().contains(literal));

        println("");
        println("Database changes   : NONE");
        println("============================================================");
    }
}