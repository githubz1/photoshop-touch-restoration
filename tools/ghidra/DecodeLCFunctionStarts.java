import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Function;

public class DecodeLCFunctionStarts extends GhidraScript {

    @Override
    public void run() throws Exception {

        Address sequenceStart = currentAddress;

        println("============================================================");
        println("LC_FUNCTION_STARTS DECODER");
        println("============================================================");
        println("Starting address : " + sequenceStart);
        println("Image base       : " + currentProgram.getImageBase());
        println();

        if (sequenceStart == null) {
            println("ERROR: No current address.");
            return;
        }

        Data firstData =
            currentProgram.getListing().getDataContaining(sequenceStart);

        if (firstData == null) {
            println("ERROR: No defined data at current address.");
            return;
        }

        if (!isULEB128(firstData)) {
            println("ERROR: Current data is not ULEB128.");
            println("Data type: " + firstData.getDataType().getName());
            return;
        }

        Address addr = firstData.getAddress();
        long cumulativeOffset = 0;
        int entryCount = 0;

        println("------------------------------------------------------------");
        println("DECODED FUNCTION STARTS");
        println("------------------------------------------------------------");

        while (true) {

            Data data =
                currentProgram.getListing().getDataContaining(addr);

            if (data == null || !isULEB128(data)) {
                println();
                println("Stopped: next item is not ULEB128 data.");
                break;
            }

            long delta;

            try {
                delta = decodeULEB128(
                    data.getAddress(),
                    data.getLength()
                );
            }
            catch (Exception e) {
                println("ERROR decoding " + data.getAddress());
                println(e.getMessage());
                break;
            }

            if (delta == 0) {
                println();
                println("Zero ULEB128 encountered; treating it as the end of the sequence.");
                break;
            }

            cumulativeOffset += delta;

            Address functionAddress =
                currentProgram.getImageBase().add(cumulativeOffset);

            Function function =
                currentProgram.getFunctionManager()
                              .getFunctionAt(functionAddress);

            String functionState;

            if (function != null) {
                functionState = function.getName();
            }
            else {
                functionState = "<no Ghidra function>";
            }

            println(String.format(
                "%-8d %-14s delta=0x%-12X cumulative=0x%-12X address=%s Ghidra=%s",
                entryCount,
                data.getAddress(),
                delta,
                cumulativeOffset,
                functionAddress,
                functionState
            ));

            entryCount++;

            Address next =
                data.getAddress().add(data.getLength());

            if (next.compareTo(currentProgram.getMaxAddress()) > 0) {
                break;
            }

            Data nextData =
                currentProgram.getListing().getDataContaining(next);

            if (nextData == null || !isULEB128(nextData)) {
                break;
            }

            if (!nextData.getAddress().equals(next)) {
                break;
            }

            addr = next;
        }

        println();
        println("============================================================");
        println("SUMMARY");
        println("============================================================");
        println("Sequence start   : " + sequenceStart);
        println("Entries decoded  : " + entryCount);
        println("Final cumulative : 0x" +
            Long.toHexString(cumulativeOffset).toUpperCase());
        println();
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

    private long decodeULEB128(Address address, int length)
        throws Exception {

        long result = 0;
        int shift = 0;

        for (int i = 0; i < length; i++) {

            int value =
                currentProgram.getMemory()
                              .getByte(address.add(i)) & 0xff;

            result |= (long)(value & 0x7f) << shift;

            if ((value & 0x80) == 0) {
                return result;
            }

            shift += 7;

            if (shift >= 64) {
                throw new Exception("ULEB128 value exceeds 64 bits.");
            }
        }

        throw new Exception("ULEB128 value did not terminate.");
    }
}