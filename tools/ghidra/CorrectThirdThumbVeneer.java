// CorrectThirdThumbVeneer.java
// PS Touch — Third ARM Thumb veneer metadata correction
//
// Corrects Ghidra function-body metadata only.
// Does not modify Adobe's original executable bytes.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

public class CorrectThirdThumbVeneer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("============================================================");
        println("PS TOUCH — THIRD VENEER FUNCTION-BODY CORRECTION");
        println("============================================================");

        Address ownerStart = toAddr("00fb8948");
        Address ownerEnd = toAddr("00fb8995");

        Address veneerStart = toAddr("01098b20");
        Address veneerSplit = toAddr("01098b22");
        Address veneerEnd = toAddr("01098b27");

        Address literalStart = toAddr("01098b28");

        FunctionManager fm = currentProgram.getFunctionManager();

        Function owner = fm.getFunctionAt(ownerStart);

        if (owner == null) {
            println("ABORT: Original function not found.");
            return;
        }

        AddressSetView body = owner.getBody();

        AddressSet expected = new AddressSet();

        expected.addRange(ownerStart, ownerEnd);
        expected.addRange(veneerStart, veneerStart);
        expected.addRange(veneerSplit, veneerEnd);

        AddressSet corrected = new AddressSet();
        corrected.addRange(ownerStart, ownerEnd);

        println("");
        println("FUNCTION");
        println("------------------------------------------------------------");

        println("Name          : " + owner.getName());
        println("Entry         : " + owner.getEntryPoint());
        println("Current size  : " + body.getNumAddresses());
        println("Expected size : " + expected.getNumAddresses());
        println("Proposed size : " + corrected.getNumAddresses());

        println("");
        println("ACTUAL BODY RANGES");
        println("------------------------------------------------------------");

        for (AddressRange range : body) {
            println(
                range.getMinAddress()
                + " - "
                + range.getMaxAddress()
                + " ("
                + range.getLength()
                + " bytes)"
            );
        }

        println("");
        println("EXPECTED BODY RANGES");
        println("------------------------------------------------------------");

        for (AddressRange range : expected) {
            println(
                range.getMinAddress()
                + " - "
                + range.getMaxAddress()
                + " ("
                + range.getLength()
                + " bytes)"
            );
        }

        println("");
        println("SAFETY CHECKS");
        println("------------------------------------------------------------");

        boolean sizeCorrect =
            body.getNumAddresses() == 85;

        boolean expectedSizeCorrect =
            expected.getNumAddresses() == 85;

        boolean rangesCorrect =
            body.hasSameAddresses(expected);

        boolean originalPreserved =
            body.contains(ownerStart, ownerEnd);

        boolean veneerOwned =
            fm.getFunctionContaining(veneerStart) == owner;

        boolean veneerNotIndependent =
            fm.getFunctionAt(veneerStart) == null;

        boolean literalExcluded =
            !body.contains(literalStart);

        println("Actual size correct   : " + sizeCorrect);
        println("Expected size correct : " + expectedSizeCorrect);
        println("Exact ranges match    : " + rangesCorrect);
        println("Original preserved    : " + originalPreserved);
        println("Veneer owned          : " + veneerOwned);
        println("No independent thunk  : " + veneerNotIndependent);
        println("Literal excluded      : " + literalExcluded);

        boolean passed =
            sizeCorrect
            && expectedSizeCorrect
            && rangesCorrect
            && originalPreserved
            && veneerOwned
            && veneerNotIndependent
            && literalExcluded;

        if (!passed) {
            println("");
            println("RESULT: ABORTED");
            println("No metadata changes performed.");
            println("============================================================");
            return;
        }

        println("");
        println("APPLYING METADATA CORRECTION");
        println("------------------------------------------------------------");

        owner.setBody(corrected);

        Function updated = fm.getFunctionAt(ownerStart);

        boolean bodyCorrect =
            updated != null
            && updated.getBody().hasSameAddresses(corrected)
            && updated.getBody().getNumAddresses() == 78;

        boolean veneerDetached =
            fm.getFunctionContaining(veneerStart) == null
            && fm.getFunctionContaining(veneerEnd) == null;

        println("");
        println("POST-CORRECTION VERIFICATION");
        println("------------------------------------------------------------");

        println("Body correct    : " + bodyCorrect);
        println("Veneer detached : " + veneerDetached);

        println("");

        if (bodyCorrect && veneerDetached) {
            println("RESULT: SUCCESS");
            println("Removed 7 incorrectly associated veneer bytes.");
        } else {
            println("RESULT: VERIFICATION FAILED");
            println("Inspect database state before proceeding.");
        }

        println("Executable bytes modified: NONE");
        println("============================================================");
    }
}