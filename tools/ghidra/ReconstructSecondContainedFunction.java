//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;

public class ReconstructSecondContainedFunction extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address destructorEntry = toAddr("00f7c6dc");
        Address functionEntry = toAddr("00f80a44");
        Address functionEnd = toAddr("00f80a6f");
        Address nextEntry = toAddr("00f80a70");
        Address branchAddress = toAddr("00f7c6f4");

        Listing listing = currentProgram.getListing();

        println("============================================================");
        println("PS TOUCH — SECOND CONTAINED FUNCTION RECONSTRUCTION");
        println("============================================================");

        Function destructor =
            currentProgram.getFunctionManager()
                .getFunctionAt(destructorEntry);

        Function existing =
            currentProgram.getFunctionManager()
                .getFunctionAt(functionEntry);

        Function owner =
            currentProgram.getFunctionManager()
                .getFunctionContaining(functionEntry);

        Function next =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry);

        boolean destructorCorrect =
            destructor != null &&
            destructor.getName().equals(".cxx_destruct") &&
            destructor.getBody().getNumAddresses() == 28 &&
            destructor.getBody().getMinAddress()
                .equals(destructorEntry) &&
            destructor.getBody().getMaxAddress()
                .equals(toAddr("00f7c6f7"));

        boolean entryFree =
            existing == null && owner == null;

        boolean nextPreserved =
            next != null &&
            next.getEntryPoint().equals(nextEntry);

        Instruction branch =
            listing.getInstructionAt(branchAddress);

        boolean branchCorrect =
            branch != null &&
            branch.getMnemonicString().equalsIgnoreCase("b.w") &&
            branch.getMaxAddress().equals(toAddr("00f7c6f7"));

        boolean incomingReference = false;

        for (Reference reference : getReferencesTo(functionEntry)) {
            if (reference.getFromAddress().equals(branchAddress) &&
                reference.getReferenceType() ==
                    RefType.UNCONDITIONAL_JUMP) {
                incomingReference = true;
            }
        }

        Instruction prologue =
            listing.getInstructionAt(functionEntry);

        boolean prologueCorrect =
            prologue != null &&
            prologue.getMnemonicString().equalsIgnoreCase("push");

        Instruction epilogue =
            listing.getInstructionAt(toAddr("00f80a6e"));

        boolean epilogueCorrect =
            epilogue != null &&
            epilogue.getMnemonicString().equalsIgnoreCase("pop") &&
            epilogue.getMaxAddress().equals(functionEnd);

        boolean rangeFree = true;
        boolean instructionsValid = true;
        long instructionBytes = 0;

        Address cursor = functionEntry;

        while (cursor.compareTo(functionEnd) <= 0) {

            Function containing =
                currentProgram.getFunctionManager()
                    .getFunctionContaining(cursor);

            if (containing != null) {
                rangeFree = false;
            }

            Instruction instruction =
                listing.getInstructionAt(cursor);

            if (instruction == null ||
                instruction.getMaxAddress()
                    .compareTo(functionEnd) > 0) {

                instructionsValid = false;
                break;
            }

            instructionBytes += instruction.getLength();
            cursor = instruction.getMaxAddress().next();
        }

        boolean coverageCorrect =
            instructionsValid &&
            instructionBytes == 44 &&
            cursor.equals(functionEnd.next());

        boolean safe =
            destructorCorrect &&
            entryFree &&
            nextPreserved &&
            branchCorrect &&
            incomingReference &&
            prologueCorrect &&
            epilogueCorrect &&
            rangeFree &&
            coverageCorrect;

        println("\nPREFLIGHT CHECKS");
        println("------------------------------------------------------------");
        println("Destructor corrected      : " + destructorCorrect);
        println("Entry unowned             : " + entryFree);
        println("Entire range unowned      : " + rangeFree);
        println("Tail branch preserved     : " + branchCorrect);
        println("Incoming reference valid  : " + incomingReference);
        println("Candidate prologue valid  : " + prologueCorrect);
        println("Candidate epilogue valid  : " + epilogueCorrect);
        println("Instruction coverage      : " + coverageCorrect);
        println("Next function preserved   : " + nextPreserved);

        if (!safe) {
            println("\nRESULT : ABORTED");
            println("Database changes : NONE");
            return;
        }

        println("\nCREATING RECOVERED FUNCTION");
        println("------------------------------------------------------------");

        Function recovered =
            createFunction(functionEntry, "FUN_00f80a44");

        if (recovered == null) {
            println("RESULT : FAIL — Function creation returned null");
            return;
        }

        AddressSet body = new AddressSet();
        body.addRange(functionEntry, functionEnd);

        recovered.setBody(body);

        Function verified =
            currentProgram.getFunctionManager()
                .getFunctionAt(functionEntry);

        boolean recoveredCorrect =
            verified != null &&
            verified.getEntryPoint().equals(functionEntry) &&
            verified.getBody().getNumAddresses() == 44 &&
            verified.getBody().getMinAddress()
                .equals(functionEntry) &&
            verified.getBody().getMaxAddress()
                .equals(functionEnd);

        boolean destructorPreserved =
            currentProgram.getFunctionManager()
                .getFunctionAt(destructorEntry)
                .getBody().getNumAddresses() == 28;

        boolean nextStillPreserved =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextEntry) != null;

        println("Function name : " +
            (verified == null ? "NONE" : verified.getName()));

        println("Entry point   : " +
            (verified == null ? "NONE" : verified.getEntryPoint()));

        println("Body size     : " +
            (verified == null
                ? "NONE"
                : verified.getBody().getNumAddresses()));

        println("\nPOST-RECONSTRUCTION VERIFICATION");
        println("------------------------------------------------------------");
        println("Recovered function correct : " + recoveredCorrect);
        println("Destructor preserved       : " + destructorPreserved);
        println("Next function preserved    : " + nextStillPreserved);

        boolean pass =
            recoveredCorrect &&
            destructorPreserved &&
            nextStillPreserved;

        println("\nRESULT : " + (pass ? "PASS" : "FAIL"));
        println("Database changes   : FUNCTION METADATA ONLY");
        println("Executable changes : NONE");
        println("============================================================");
    }
}