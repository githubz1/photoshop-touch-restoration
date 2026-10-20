import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Listing;

public class FindULEB128SequenceStart extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address current = currentAddress;
        Listing listing = currentProgram.getListing();

        println("============================================================");
        println("ULEB128 FUNCTION SEQUENCE START FINDER");
        println("============================================================");
        println("Starting address: " + current);
        println();

        Data currentData = listing.getDataContaining(current);

        if (currentData == null) {
            println("ERROR: No defined data contains " + current);
            return;
        }

        if (!isULEB128(currentData)) {
            println("ERROR: Current data is not ULEB128.");
            println("Data type: " + currentData.getDataType().getName());
            return;
        }

        println("Current data:");
        println("  Address : " + currentData.getAddress());
        println("  Length  : " + currentData.getLength());
        println("  Type    : " + currentData.getDataType().getName());
        println("  Value   : " + currentData.getValue());
        println();

        // --------------------------------------------------------
        // Walk backward through contiguous ULEB128 data.
        // --------------------------------------------------------

        Address sequenceStart = currentData.getAddress();
        int backwardCount = 0;

        Address probe = sequenceStart.subtract(1);

        while (probe.compareTo(currentProgram.getMinAddress()) >= 0) {

            Data previousData = listing.getDataContaining(probe);

            if (previousData == null) {
                break;
            }

            if (!isULEB128(previousData)) {
                break;
            }

            sequenceStart = previousData.getAddress();
            backwardCount++;

            probe = sequenceStart.subtract(1);
        }

        // --------------------------------------------------------
        // Report the boundary.
        // --------------------------------------------------------

        println("------------------------------------------------------------");
        println("DETECTED SEQUENCE");
        println("------------------------------------------------------------");
        println("Current entry      : " + currentData.getAddress());
        println("Sequence start     : " + sequenceStart);
        println("Entries walked back: " + backwardCount);
        println();

        // --------------------------------------------------------
        // Examine what comes immediately before the sequence.
        // --------------------------------------------------------

        Address before = sequenceStart.subtract(1);
        Data beforeData = listing.getDataContaining(before);

        println("IMMEDIATELY BEFORE SEQUENCE");
        println("------------------------------------------------------------");

        if (beforeData == null) {
            println("No defined data.");
        }
        else {
            println("Address : " + beforeData.getAddress());
            println("Length  : " + beforeData.getLength());
            println("Type    : " + beforeData.getDataType().getName());
            println("Value   : " + beforeData.getValue());
        }

        println();

        // --------------------------------------------------------
        // Walk forward and print the beginning of the sequence.
        // --------------------------------------------------------

        println("FIRST 50 ULEB128 ENTRIES");
        println("------------------------------------------------------------");

        Address addr = sequenceStart;
        int count = 0;

        while (count < 50) {

            Data data = listing.getDataContaining(addr);

            if (data == null || !isULEB128(data)) {
                break;
            }

            println(String.format(
                "%s  %-8s  length=%-2d  value=%s",
                data.getAddress(),
                data.getDataType().getName(),
                data.getLength(),
                data.getValue()
            ));

            count++;

            Address next = data.getAddress().add(data.getLength());

            if (next.compareTo(currentProgram.getMaxAddress()) > 0) {
                break;
            }

            Data nextData = listing.getDataContaining(next);

            if (nextData == null || !isULEB128(nextData)) {
                break;
            }

            // Make sure there is no gap.
            if (!nextData.getAddress().equals(next)) {
                break;
            }

            addr = next;
        }

        println();
        println("------------------------------------------------------------");
        println("Forward entries printed: " + count);
        println("------------------------------------------------------------");
        println("Done.");
    }

    private boolean isULEB128(Data data) {

        if (data == null) {
            return false;
        }

        String typeName = data.getDataType().getName();

        if (typeName == null) {
            return false;
        }

        return typeName.equalsIgnoreCase("ULEB128")
            || typeName.toUpperCase().contains("ULEB128");
    }
}
