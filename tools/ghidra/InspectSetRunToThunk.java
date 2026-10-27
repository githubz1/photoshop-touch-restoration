import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;

public class InspectSetRunToThunk extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address entry = toAddr("01098b08");
        Address expectedEnd = toAddr("01098b0f");
        Address expectedTarget = toAddr("00f7c3f0");
        Address literal = toAddr("01098b10");

        Function function =
            currentProgram.getFunctionManager().getFunctionAt(entry);

        println("============================================================");
        println("PS TOUCH — THUNK INSPECTION (READ ONLY)");
        println("============================================================");

        if (function == null) {
            println("FAIL: No function at " + entry);
            return;
        }

        println("Function name    : " + function.getName());
        println("Entry point      : " + function.getEntryPoint());
        println("Is thunk         : " + function.isThunk());
        println("Body size        : " + function.getBody().getNumAddresses());

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
        println("THUNK DESTINATION");
        println("------------------------------------------------------------");

        Function target = function.isThunk()
            ? function.getThunkedFunction(false)
            : null;

        println("Destination      : " +
            (target == null ? "NONE" : target.getEntryPoint()));

        println("Destination name : " +
            (target == null ? "NONE" : target.getName()));

        boolean bodyCorrect =
            function.getBody().getNumAddresses() == 8 &&
            count == 1 &&
            function.getBody().contains(entry, expectedEnd);

        boolean targetCorrect =
            target != null &&
            target.getEntryPoint().equals(expectedTarget);

        boolean literalExcluded =
            !function.getBody().contains(literal);

        boolean passed =
            function.isThunk() &&
            bodyCorrect &&
            targetCorrect &&
            literalExcluded;

        println("");
        println("VERIFICATION");
        println("------------------------------------------------------------");
        println("Body correct     : " + bodyCorrect);
        println("Target correct   : " + targetCorrect);
        println("Literal excluded : " + literalExcluded);
        println("RESULT           : " + (passed ? "PASS" : "FAIL"));
        println("Database changes : NONE");
        println("============================================================");
    }
}