// Import original Photoshop Touch Mach-O symbols.
// @category PhotoshopTouch

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.model.symbol.SymbolTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ImportPSTouchSymbols extends GhidraScript {

    private String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "_unnamed";
        }

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if ((c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') ||
                c == '_' ||
                c == '$' ||
                c == '.') {

                out.append(c);
            }
            else {
                out.append('_');
            }
        }

        String result = out.toString();

        // Collapse repeated underscores for readability.
        while (result.contains("__")) {
            result = result.replace("__", "_");
        }

        if (result.isEmpty()) {
            return "_unnamed";
        }

        return result;
    }

    private boolean isAutoName(String name) {
        if (name == null) {
            return true;
        }

        return name.startsWith("FUN_") ||
               name.startsWith("LAB_") ||
               name.startsWith("DAT_") ||
               name.startsWith("PTR_") ||
               name.startsWith("UNK_") ||
               name.startsWith("SUB_");
    }

    private boolean symbolAlreadyExists(
            SymbolTable table,
            Address address,
            String name) {

        Symbol[] symbols = table.getSymbols(address);

        for (Symbol symbol : symbols) {
            if (name.equals(symbol.getName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void run() throws Exception {

        File symbolFile = new File(
            System.getProperty("user.home") +
            "/Admin/photoshop-touch-restoration/tools/PSTouchSymbols.tsv"
        );

        if (!symbolFile.isFile()) {
            printerr(
                "Symbol file not found: " +
                symbolFile.getAbsolutePath()
            );
            return;
        }

        int total = 0;
        int renamedFunctions = 0;
        int createdLabels = 0;
        int alreadyPresent = 0;
        int manualPreserved = 0;
        int outsideMemory = 0;
        int failures = 0;

        SymbolTable table = currentProgram.getSymbolTable();

        println("============================================================");
        println("Photoshop Touch Mach-O symbol import");
        println("Source: " + symbolFile.getAbsolutePath());
        println("============================================================");

        BufferedReader reader =
            new BufferedReader(new FileReader(symbolFile));

        try {
            String line;

            while ((line = reader.readLine()) != null) {

                if (monitor.isCancelled()) {
                    println("Cancelled.");
                    break;
                }

                if (line.isEmpty()) {
                    continue;
                }

                int tab = line.indexOf('\t');

                if (tab <= 0) {
                    failures++;
                    continue;
                }

                String addressString =
                    line.substring(0, tab);

                String rawName =
                    line.substring(tab + 1);

                String originalName =
                    sanitizeName(rawName);

                total++;

                try {
                    Address address =
                        toAddr("0x" + addressString);

                    if (address == null ||
                        !currentProgram.getMemory().contains(address)) {

                        outsideMemory++;
                        continue;
                    }

                    Function function =
                        currentProgram
                            .getFunctionManager()
                            .getFunctionAt(address);

                    if (function != null) {

                        String currentName =
                            function.getName();

                        if (originalName.equals(currentName)) {
                            alreadyPresent++;
                            continue;
                        }

                        if (isAutoName(currentName)) {
                            try {
                                function.setName(
                                    originalName,
                                    SourceType.IMPORTED
                                );

                                renamedFunctions++;
                                continue;

                            } catch (Exception e) {
                                // Try secondary label below.
                            }
                        }
                        else {
                            manualPreserved++;
                        }
                    }

                    if (symbolAlreadyExists(
                            table,
                            address,
                            originalName)) {

                        alreadyPresent++;
                        continue;
                    }

                    try {
                        table.createLabel(
                            address,
                            originalName,
                            SourceType.IMPORTED
                        );

                        createdLabels++;

                    } catch (Exception e) {
                        failures++;

                        println(
                            "FAILED 0x" +
                            addressString +
                            " " +
                            rawName +
                            " -> " +
                            originalName +
                            " : " +
                            e.getMessage()
                        );
                    }

                } catch (Exception e) {
                    failures++;

                    println(
                        "FAILED 0x" +
                        addressString +
                        " " +
                        rawName +
                        " -> " +
                        originalName +
                        " : " +
                        e.getMessage()
                    );
                }
            }

        } finally {
            reader.close();
        }

        println("");
        println("============================================================");
        println("IMPORT COMPLETE");
        println("============================================================");
        println("Symbols processed : " + total);
        println("Functions renamed : " + renamedFunctions);
        println("Labels created    : " + createdLabels);
        println("Already present   : " + alreadyPresent);
        println("Manual preserved  : " + manualPreserved);
        println("Outside memory    : " + outsideMemory);
        println("Failures          : " + failures);
        println("============================================================");
    }
}
