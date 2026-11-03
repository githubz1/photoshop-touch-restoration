//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;

public class InspectThirdContainedFunction extends GhidraScript {

    @Override
    protected void run() throws Exception {

        Address original = toAddr("00f8099c");
        Address candidate = toAddr("00f80cc4");

        FunctionManager manager =
            currentProgram.getFunctionManager();

        Function atOriginal = manager.getFunctionAt(original);
        Function owner = manager.getFunctionContaining(candidate);
        Function atCandidate = manager.getFunctionAt(candidate);

        println("============================================================");
        println("PS TOUCH — THIRD CONTAINED FUNCTION INSPECTION");
        println("============================================================");

        println("\nORIGINAL FUNCTION");
        println("------------------------------------------------------------");

        if (atOriginal == null) {
            println("Original function: NONE");
        } else {

            println("Name      : " + atOriginal.getName());
            println("Entry     : " + atOriginal.getEntryPoint());
            println("Body size : " +
                atOriginal.getBody().getNumAddresses());

            println("\nBODY RANGES");
            println("------------------------------------------------------------");

            int index = 0;

            for (AddressRange range : atOriginal.getBody()) {
                println(
                    (++index) + ". " +
                    range.getMinAddress() + " - " +
                    range.getMaxAddress() +
                    " (" + range.getLength() + " bytes)"
                );
            }
        }

        println("\nCANDIDATE ENTRY");
        println("------------------------------------------------------------");

        println("Address              : " + candidate);

        println("Independent function : " +
            (atCandidate == null
                ? "NONE"
                : atCandidate.getName()));

        println("Owning function      : " +
            (owner == null
                ? "NONE"
                : owner.getName()));

        println("Owner entry          : " +
            (owner == null
                ? "NONE"
                : owner.getEntryPoint()));

        Instruction instruction =
            currentProgram.getListing().getInstructionAt(candidate);

        println("Instruction          : " +
            (instruction == null
                ? "NONE"
                : instruction.toString()));

        println("\nINCOMING REFERENCES");
        println("------------------------------------------------------------");

        Reference[] references = getReferencesTo(candidate);

        println("Reference count : " + references.length);

        for (Reference reference : references) {
            println(
                reference.getFromAddress() + " -> " +
                reference.getToAddress() +
                " [" + reference.getReferenceType() + "]"
            );
        }

        println("\nNEARBY INSTRUCTIONS");
        println("------------------------------------------------------------");

        Address start = candidate.subtract(16);
        Address end = candidate.add(64);

        Instruction current =
            currentProgram.getListing().getInstructionAt(start);

        if (current == null) {
            current =
                currentProgram.getListing()
                    .getInstructionAfter(start);
        }

        while (current != null &&
               current.getAddress().compareTo(end) <= 0) {

            println(
                current.getAddress() + "  " +
                current.toString()
            );

            current =
                currentProgram.getListing()
                    .getInstructionAfter(current.getAddress());
        }

        println("\nDatabase changes : NONE");
        println("============================================================");
    }
}