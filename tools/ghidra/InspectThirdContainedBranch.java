//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;

public class InspectThirdContainedBranch extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address originalEntry = toAddr("00f8099c");
        Address branchAddress = toAddr("00f809a4");
        Address candidateEntry = toAddr("00f80cc4");
        Address candidateEnd = toAddr("00f80d19");
        Address nextAddress = toAddr("00f80d1c");

        println("============================================================");
        println("PS TOUCH — THIRD CONTAINED FUNCTION BRANCH INSPECTION");
        println("============================================================");

        Function original =
            currentProgram.getFunctionManager()
                .getFunctionAt(originalEntry);

        println("\nORIGINAL FUNCTION");
        println("------------------------------------------------------------");

        println("Name  : " +
            (original == null ? "NONE" : original.getName()));

        println("Entry : " + originalEntry);

        println("\nORIGINAL FUNCTION INSTRUCTIONS");
        println("------------------------------------------------------------");

        Instruction instruction =
            currentProgram.getListing()
                .getInstructionAt(originalEntry);

        while (instruction != null &&
               instruction.getAddress()
                   .compareTo(toAddr("00f809c1")) <= 0) {

            println(
                instruction.getAddress() + "  " +
                instruction.toString()
            );

            instruction =
                currentProgram.getListing()
                    .getInstructionAfter(instruction.getAddress());
        }

        println("\nCONDITIONAL BRANCH");
        println("------------------------------------------------------------");

        Instruction branch =
            currentProgram.getListing()
                .getInstructionAt(branchAddress);

        println("Address     : " + branchAddress);
        println("Instruction : " +
            (branch == null ? "NONE" : branch.toString()));

        if (branch != null) {
            println("Length      : " + branch.getLength());
            println("Fallthrough : " + branch.getFallThrough());
        }

        println("\nREFERENCES FROM BRANCH");
        println("------------------------------------------------------------");

        for (Reference reference : getReferencesFrom(branchAddress)) {
            println(
                reference.getFromAddress() + " -> " +
                reference.getToAddress() +
                " [" + reference.getReferenceType() + "]"
            );
        }

        println("\nCANDIDATE FUNCTION END");
        println("------------------------------------------------------------");

        Address endStart = toAddr("00f80d04");

        instruction =
            currentProgram.getListing()
                .getInstructionAt(endStart);

        if (instruction == null) {
            instruction =
                currentProgram.getListing()
                    .getInstructionAfter(endStart);
        }

        while (instruction != null &&
               instruction.getAddress()
                   .compareTo(candidateEnd) <= 0) {

            println(
                instruction.getAddress() + "  " +
                instruction.toString()
            );

            instruction =
                currentProgram.getListing()
                    .getInstructionAfter(instruction.getAddress());
        }

        println("\nFOLLOWING FUNCTION");
        println("------------------------------------------------------------");

        Function next =
            currentProgram.getFunctionManager()
                .getFunctionAt(nextAddress);

        println("Address  : " + nextAddress);
        println("Function : " +
            (next == null ? "NONE" : next.getName()));

        Instruction nextInstruction =
            currentProgram.getListing()
                .getInstructionAt(nextAddress);

        println("Instruction : " +
            (nextInstruction == null
                ? "NONE"
                : nextInstruction.toString()));

        println("\nDatabase changes : NONE");
        println("============================================================");
    }
}