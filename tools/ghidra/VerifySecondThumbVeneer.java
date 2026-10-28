import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;

public class VerifySecondThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("00f80bc0");
        Address originalEnd = toAddr("00f80bd7");
        Address veneerStart = toAddr("01098b14");
        Address veneerEnd = toAddr("01098b1b");
        Address literal = toAddr("01098b1c");

        Function function = currentProgram
            .getFunctionManager()
            .getFunctionAt(entry);

        println("============================================================");
        println("PS TOUCH — SECOND VENEER CORRECTION PREFLIGHT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        if (function == null) {
            println("FAIL: Original function not found.");
            return;
        }

        println("Function name : " + function.getName());
        println("Entry point   : " + function.getEntryPoint());
        println("Current size  : " + function.getBody().getNumAddresses());

        println("");
        println("EXISTING BODY RANGES");
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

        boolean expectedRanges =
            count == 3 &&
            function.getBody().getNumAddresses() == 31 &&
            function.getBody().contains(entry, originalEnd) &&
            function.getBody().contains(veneerStart) &&
            function.getBody().contains(toAddr("01098b16"), veneerEnd) &&
            !function.getBody().contains(toAddr("01098b15"));

        boolean originalPreserved =
            function.getBody().contains(entry, originalEnd);

        boolean veneerIsNotIndependent =
            currentProgram.getFunctionManager()
                .getFunctionAt(veneerStart) == null;

        boolean literalExcluded =
            !function.getBody().contains(literal);

        boolean passed =
            expectedRanges &&
            originalPreserved &&
            veneerIsNotIndependent &&
            literalExcluded;

        println("");
        println("PREFLIGHT CHECKS");
        println("------------------------------------------------------------");

        println("Expected original ranges : " + expectedRanges);
        println("Original body preserved  : " + originalPreserved);
        println("Veneer not independent   : " + veneerIsNotIndependent);
        println("Literal excluded         : " + literalExcluded);

        println("");
        println("PROPOSED CORRECTION");
        println("------------------------------------------------------------");
        println("Preserve : 00f80bc0 - 00f80bd7 (24 bytes)");
        println("Detach   : 7 veneer bytes");
        println("New size : 24 bytes");

        println("");
        println("RESULT           : " + (passed ? "PASS" : "FAIL"));
        println("Database changes : NONE");
        println("============================================================");
    }
}