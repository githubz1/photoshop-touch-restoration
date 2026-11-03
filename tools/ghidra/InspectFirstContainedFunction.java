//@category PS Touch
//@author PS Touch Restoration

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressRange;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;

public class InspectFirstContainedFunction extends GhidraScript {

    @Override
    protected void run() throws Exception {

        Address original = toAddr("00df81dc");
        Address candidate = toAddr("00df81e8");

        FunctionManager manager =
            currentProgram.getFunctionManager();

        Function owner = manager.getFunctionContaining(candidate);
        Function atOriginal = manager.getFunctionAt(original);
        Function atCandidate = manager.getFunctionAt(candidate);

        println("============================================================");
        println("PS TOUCH — FIRST CONTAINED FUNCTION INSPECTION");
        println("============================================================");

        println("\nORIGINAL FUNCTION");
        println("------------------------------------------------------------");

        if (atOriginal == null) {
            println("Original function: NONE");
        } else {
            println("Name       : " + atOriginal.getName());
            println("Entry      : " + atOriginal.getEntryPoint());
            println("Body size  : " + atOriginal.getBody().getNumAddresses());

            println("\nBODY RANGES");

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
            (atCandidate == null ? "NONE" : atCandidate.getName()));
        println("Owning function      : " +
            (owner == null ? "NONE" : owner.getName()));

        Instruction instruction =
            currentProgram.getListing().getInstructionAt(candidate);

        println("Instruction          : " +
            (instruction == null ? "NONE" : instruction.toString()));

        println("\nINCOMING REFERENCES");
        println("------------------------------------------------------------");

        Reference[] references =
            getReferencesTo(candidate);

        println("Reference count : " + references.length);

        for (Reference reference : references) {
            println(
                reference.getFromAddress() + " -> " +
                reference.getToAddress() +
                " [" + reference.getReferenceType() + "]"
            );
        }

        println("\nDatabase changes   : NONE");
        println("Executable changes : NONE");
        println("============================================================");
    }
}