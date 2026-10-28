// VerifyThirdThumbVeneer.java
// PS Touch — Third ARM Thumb veneer correction preflight
// READ ONLY — No database modifications

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class VerifyThirdThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — THIRD VENEER CORRECTION PREFLIGHT");
        println("READ ONLY — NO DATABASE MODIFICATIONS");
        println("============================================================");

        Address ownerStart = toAddr("00fb8948");
        Address ownerEnd = toAddr("00fb8995");

        Address veneerStart = toAddr("01098b20");
        Address veneerEnd = toAddr("01098b27");
        Address literalStart = toAddr("01098b28");

        FunctionManager fm = currentProgram.getFunctionManager();
        Function owner = fm.getFunctionAt(ownerStart);

        if (owner == null) {
            println("RESULT: FAIL — Original function missing.");
            return;
        }

        AddressSetView body = owner.getBody();

        println("Function name : " + owner.getName());
        println("Entry point   : " + owner.getEntryPoint());
        println("Current size  : " + body.getNumAddresses());

        println("");
        println("EXISTING BODY RANGES");
        println("------------------------------------------------------------");

        int index = 1;

        for (AddressRange range : body) {

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

        boolean expectedRanges =
            body.getNumAddressRanges() == 3
            && body.getNumAddresses() == 85
            && body.contains(ownerStart, ownerEnd)
            && body.contains(veneerStart)
            && body.contains(toAddr("01098b22"), veneerEnd);

        boolean originalPreserved =
            body.contains(ownerStart, ownerEnd);

        boolean veneerNotIndependent =
            fm.getFunctionAt(veneerStart) == null;

        boolean literalExcluded =
            !body.contains(literalStart);

        boolean ownerCorrect =
            fm.getFunctionContaining(veneerStart) == owner;

        println("");
        println("PREFLIGHT CHECKS");
        println("------------------------------------------------------------");

        println("Expected original ranges : " + expectedRanges);
        println("Original body preserved  : " + originalPreserved);
        println("Veneer not independent   : " + veneerNotIndependent);
        println("Literal excluded         : " + literalExcluded);
        println("Owner correct            : " + ownerCorrect);

        boolean passed =
            expectedRanges
            && originalPreserved
            && veneerNotIndependent
            && literalExcluded
            && ownerCorrect;

        println("");
        println("PROPOSED CORRECTION");
        println("------------------------------------------------------------");

        println("Preserve : " + ownerStart + " - " + ownerEnd);
        println("Detach   : 7 veneer bytes");
        println("New size : 78 bytes");

        println("");
        println("RESULT           : " + (passed ? "PASS" : "FAIL"));
        println("Database changes : NONE");
        println("============================================================");
    }
}