package
{
    import flash.display.Sprite;
    import flash.text.TextField;
    import flash.text.TextFormat;
    import flash.text.TextFieldAutoSize;
    import flash.events.MouseEvent;
    import flash.external.ExtensionContext;
    import flash.utils.ByteArray;
    import flash.utils.Endian;
    import com.adobe.ttpixel.extension.TTQuickSelection;

    public class TTPixelANETest extends Sprite
    {
        public function TTPixelANETest()
        {
            super();

            graphics.beginFill(0xFF0000);
            graphics.drawRect(0, 0, 2000, 2000);
            graphics.endFill();

            var output:TextField = new TextField();
            output.defaultTextFormat = new TextFormat(
                "_typewriter",
                20,
                0x000000
            );
            output.autoSize = TextFieldAutoSize.NONE;
            output.multiline = true;
            output.wordWrap = false;
            output.x = 20;
            output.y = 20;
            output.width = stage.stageWidth - 40;
            output.height = stage.stageHeight - 40;
            output.mouseEnabled = true;
            output.selectable = true;

            addChild(output);

            // Simple touch-friendly paging for the diagnostic console.
            // Tap anywhere on the text to advance approximately one screen.
            output.addEventListener(
                MouseEvent.CLICK,
                function(e:MouseEvent):void
                {
                    if (output.scrollV >= output.maxScrollV)
                    {
                        output.scrollV = 1;
                    }
                    else
                    {
                        var next:int = output.scrollV + 12;

                        if (next > output.maxScrollV)
                            next = output.maxScrollV;

                        output.scrollV = next;
                    }
                }
            );

            output.text = "TTPixel ANE Load Test\n\n";
            output.appendText("AIR started: PASS\n");

            // Original Photoshop Touch TTNEUtils startup path:
            //
            // ExtensionContext:
            //   com.adobe.ttpixel.extension / utils
            //
            // SYSINFO_HW_MEMORY = 1
            // Return value is physical memory in KiB.
            var utilsContext:ExtensionContext =
                ExtensionContext.createExtensionContext(
                    "com.adobe.ttpixel.extension",
                    "utils"
                );

            output.appendText(
                "UTILS CTX: " +
                (utilsContext != null) +
                "\n"
            );

            if (utilsContext != null)
            {
                var hardwareMemory:Object =
                    utilsContext.call(
                        "getSystemInfo",
                        1
                    );

                output.appendText(
                    "HW MEMORY KiB: " +
                    hardwareMemory +
                    "\n"
                );

                output.appendText(
                    "UTILS getSystemInfo: " +
                    (
                        hardwareMemory != null &&
                        Number(hardwareMemory) > 0
                    ) +
                    "\n"
                );

                utilsContext.dispose();
            }

            // Original Photoshop Touch TTGL startup path:
            //
            // ExtensionContext:
            //   com.adobe.ttpixel.extension / gl
            //
            // createContext()
            // getStringInfo(kStringInfoKeyGLRenderer = 5)
            // destroyContext()
            var glContext:ExtensionContext =
                ExtensionContext.createExtensionContext(
                    "com.adobe.ttpixel.extension",
                    "gl"
                );

            output.appendText(
                "GL CTX: " +
                (glContext != null) +
                "\n"
            );

            if (glContext != null)
            {
                var glCreateStatus:int =
                    int(glContext.call("createContext"));

                output.appendText(
                    "GL CREATE STATUS: " +
                    glCreateStatus +
                    "\n"
                );

                var glRenderer:Object =
                    glContext.call(
                        "getStringInfo",
                        5
                    );

                output.appendText(
                    "GL RENDERER: " +
                    glRenderer +
                    "\n"
                );

                var glDestroyStatus:int =
                    int(glContext.call("destroyContext"));

                output.appendText(
                    "GL DESTROY STATUS: " +
                    glDestroyStatus +
                    "\n"
                );

                var glStartupPass:Boolean =
                    glCreateStatus == 0 &&
                    glRenderer != null &&
                    String(glRenderer).length > 0 &&
                    glDestroyStatus == 0;

                output.appendText(
                    "GL STARTUP: " +
                    (glStartupPass ? "PASS" : "FAIL") +
                    "\n"
                );

                glContext.dispose();
            }

            var qsClass:Class = TTQuickSelection;
            output.appendText("CLASS: " + (qsClass != null) + "\n");

            var qsContext:ExtensionContext = TTQuickSelection.getExtensionContext();
            output.appendText("CTX: " + (qsContext != null) + "\n");
            var freResult:Boolean = TTQuickSelection.disposeQuickSelectTool();
            output.appendText("FRE: " + freResult + "\n");

            // Same deterministic 10x6 fixture as
            // native/quickselection/test_quickselection.cpp.
            //
            // Left half:  RGB(20,20,20)
            // Right half: RGB(235,235,235)
            var width:int = 10;
            var height:int = 6;
            var pixels:ByteArray = new ByteArray();

            var x:int;
            var y:int;
            var value:int;

            for (y = 0; y < height; ++y)
            {
                for (x = 0; x < width; ++x)
                {
                    value = (x < 5) ? 20 : 235;

                    // Native createQuickSelectTool reads packed bytes
                    // [1], [2], [3] as R, G, B. Byte 0 is ignored.
                    pixels.writeByte(0xFF);
                    pixels.writeByte(value);
                    pixels.writeByte(value);
                    pixels.writeByte(value);
                }
            }

            pixels.position = 0;


            var createResult:Boolean =
                TTQuickSelection.createQuickSelectTool(
                    pixels,
                    width,
                    height
                );


            output.appendText("CREATE: " + createResult + "\n");

            // Foreground/add stroke on dark half.

            var fgResult:ByteArray =
                TTQuickSelection.brushHit(2, 3, 1, 0, false);


            output.appendText("FG: " + (fgResult == null) + "\n");

            // Background/remove stroke on bright half.
            // Request the resulting mask from this second stroke.

            var mask:ByteArray =
                TTQuickSelection.brushHit(7, 3, 1, 1, true);


            output.appendText("BG/MASK: " + (mask != null) + "\n");
            output.appendText(
                "MASKLEN: " + (mask != null ? mask.length : -1) + "\n"
            );

            if (mask != null)
            {
                mask.position = 0;

                var matches:Boolean = (mask.length == width * height);
                var compact:String = "";
                var bytes:String = "";

                for (y = 0; y < height; ++y)
                {
                    var maskRow:String = "";
                    var hexRow:String = "";

                    for (x = 0; x < width; ++x)
                    {
                        var b:int = mask.readUnsignedByte();
                        var expected:int = (x < 5) ? 0xFF : 0x00;

                        if (b != expected)
                            matches = false;

                        maskRow += (b != 0) ? "#" : ".";

                        var hex:String = b.toString(16).toUpperCase();
                        if (hex.length < 2)
                            hex = "0" + hex;

                        hexRow += hex;
                    }

                    compact += maskRow + "\n";
                    bytes += hexRow + "\n";
                }

                output.appendText("MASK MATCH: " + matches + "\n");

                var freshMismatch:Object =
                    qsContext.call("getFreshCutMismatchCount");

                output.appendText(
                    "FRESH CUT MISMATCHES: " +
                    freshMismatch +
                    "\n"
                );

                var refinementChanged:Object =
                    qsContext.call(
                        "getRefinementChangedPixelCount"
                    );

                output.appendText(
                    "REFINEMENT CHANGED PIXELS: " +
                    refinementChanged +
                    "\n"
                );

                var bkSelfTest:Object =
                    qsContext.call(
                        "getDiagnosticSelfTestStatus"
                    );

                output.appendText(
                    "BK SELF-TEST: " +
                    bkSelfTest +
                    "\n"
                );

                var terminalCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticTerminalCapacities"
                    ) as ByteArray;

                if (terminalCaps != null)
                {
                    terminalCaps.endian = Endian.LITTLE_ENDIAN;
                    terminalCaps.position = 0;

                    output.appendText(
                        "TERMINALS S/T (" +
                        terminalCaps.length +
                        " bytes):\n"
                    );

                    for (y = 0; y < height; ++y)
                    {
                        var terminalRow:String = "";

                        for (x = 0; x < width; ++x)
                        {
                            var sourceCap:Number =
                                terminalCaps.readFloat();

                            var sinkCap:Number =
                                terminalCaps.readFloat();

                            terminalRow +=
                                sourceCap.toFixed(1) +
                                "/" +
                                sinkCap.toFixed(1);

                            if (x + 1 < width)
                                terminalRow += " ";
                        }

                        output.appendText(
                            terminalRow + "\n"
                        );
                    }
                }
                else
                {
                    output.appendText(
                        "TERMINALS: null\n"
                    );
                }

                // -------------------------------------------------
                // Adobe ARMv7 00FFD670 mode-switch diagnostic.
                //
                // Compare capacities REQUESTED by switchMode()
                // against capacities actually ASSIGNED to the graph.
                // -------------------------------------------------

                var modeOrdinaryIndex:int = 0; // pixel (0,0)
                var modeByteOffset:int =
                    modeOrdinaryIndex * 2 * 4;

                var bgRequestedSource:Number = NaN;
                var bgRequestedSink:Number = NaN;
                var bgBeforeSource:Number = NaN;
                var bgBeforeSourceResidual:Number = NaN;
                var bgBeforeSink:Number = NaN;
                var bgBeforeSinkResidual:Number = NaN;
                var bgAfterSource:Number = NaN;
                var bgAfterSourceResidual:Number = NaN;
                var bgAfterSink:Number = NaN;
                var bgAfterSinkResidual:Number = NaN;
                var bgAssignedSource:Number = NaN;
                var bgAssignedSink:Number = NaN;

                var fgRequestedSource:Number = NaN;
                var fgRequestedSink:Number = NaN;
                var fgBeforeSource:Number = NaN;
                var fgBeforeSourceResidual:Number = NaN;
                var fgBeforeSink:Number = NaN;
                var fgBeforeSinkResidual:Number = NaN;
                var fgAfterSource:Number = NaN;
                var fgAfterSourceResidual:Number = NaN;
                var fgAfterSink:Number = NaN;
                var fgAfterSinkResidual:Number = NaN;
                var fgAssignedSource:Number = NaN;
                var fgAssignedSink:Number = NaN;

                // Background mode.
                qsContext.call("switchMode", 1);

                var bgRequestedCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticRequestedTerminalCapacities"
                    ) as ByteArray;

                if (bgRequestedCaps != null)
                {
                    bgRequestedCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    if (bgRequestedCaps.length >=
                        modeByteOffset + 8)
                    {
                        bgRequestedCaps.position =
                            modeByteOffset;

                        bgRequestedSource =
                            bgRequestedCaps.readFloat();

                        bgRequestedSink =
                            bgRequestedCaps.readFloat();
                    }
                }

                var bgUpdateCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticUpdateTweightsCapacities"
                    ) as ByteArray;

                if (bgUpdateCaps != null)
                {
                    bgUpdateCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    var bgUpdateByteOffset:int =
                        modeOrdinaryIndex * 8 * 4;

                    if (bgUpdateCaps.length >=
                        bgUpdateByteOffset + 32)
                    {
                        bgUpdateCaps.position =
                            bgUpdateByteOffset;

                        bgBeforeSource =
                            bgUpdateCaps.readFloat();

                        bgBeforeSourceResidual =
                            bgUpdateCaps.readFloat();

                        bgBeforeSink =
                            bgUpdateCaps.readFloat();

                        bgBeforeSinkResidual =
                            bgUpdateCaps.readFloat();

                        bgAfterSource =
                            bgUpdateCaps.readFloat();

                        bgAfterSourceResidual =
                            bgUpdateCaps.readFloat();

                        bgAfterSink =
                            bgUpdateCaps.readFloat();

                        bgAfterSinkResidual =
                            bgUpdateCaps.readFloat();
                    }
                }

                // Refresh the assigned-capacity diagnostic snapshot.
                var bgModeMask:ByteArray =
                    qsContext.call("getMask") as ByteArray;

                var bgAssignedCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticTerminalCapacities"
                    ) as ByteArray;

                if (bgAssignedCaps != null)
                {
                    bgAssignedCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    if (bgAssignedCaps.length >=
                        modeByteOffset + 8)
                    {
                        bgAssignedCaps.position =
                            modeByteOffset;

                        bgAssignedSource =
                            bgAssignedCaps.readFloat();

                        bgAssignedSink =
                            bgAssignedCaps.readFloat();
                    }
                }

                // Return to foreground mode.
                qsContext.call("switchMode", 0);

                var fgRequestedCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticRequestedTerminalCapacities"
                    ) as ByteArray;

                if (fgRequestedCaps != null)
                {
                    fgRequestedCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    if (fgRequestedCaps.length >=
                        modeByteOffset + 8)
                    {
                        fgRequestedCaps.position =
                            modeByteOffset;

                        fgRequestedSource =
                            fgRequestedCaps.readFloat();

                        fgRequestedSink =
                            fgRequestedCaps.readFloat();
                    }
                }

                var fgUpdateCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticUpdateTweightsCapacities"
                    ) as ByteArray;

                if (fgUpdateCaps != null)
                {
                    fgUpdateCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    var fgUpdateByteOffset:int =
                        modeOrdinaryIndex * 8 * 4;

                    if (fgUpdateCaps.length >=
                        fgUpdateByteOffset + 32)
                    {
                        fgUpdateCaps.position =
                            fgUpdateByteOffset;

                        fgBeforeSource =
                            fgUpdateCaps.readFloat();

                        fgBeforeSourceResidual =
                            fgUpdateCaps.readFloat();

                        fgBeforeSink =
                            fgUpdateCaps.readFloat();

                        fgBeforeSinkResidual =
                            fgUpdateCaps.readFloat();

                        fgAfterSource =
                            fgUpdateCaps.readFloat();

                        fgAfterSourceResidual =
                            fgUpdateCaps.readFloat();

                        fgAfterSink =
                            fgUpdateCaps.readFloat();

                        fgAfterSinkResidual =
                            fgUpdateCaps.readFloat();
                    }
                }

                var fgModeMask:ByteArray =
                    qsContext.call("getMask") as ByteArray;

                var fgAssignedCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticTerminalCapacities"
                    ) as ByteArray;

                if (fgAssignedCaps != null)
                {
                    fgAssignedCaps.endian =
                        Endian.LITTLE_ENDIAN;

                    if (fgAssignedCaps.length >=
                        modeByteOffset + 8)
                    {
                        fgAssignedCaps.position =
                            modeByteOffset;

                        fgAssignedSource =
                            fgAssignedCaps.readFloat();

                        fgAssignedSink =
                            fgAssignedCaps.readFloat();
                    }
                }

                output.appendText(
                    "MODE BG BEFORE S A/R: " +
                    bgBeforeSource.toFixed(3) + "/" +
                    bgBeforeSourceResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE BG BEFORE T A/R: " +
                    bgBeforeSink.toFixed(3) + "/" +
                    bgBeforeSinkResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE BG REQUESTED S/T: " +
                    bgRequestedSource.toFixed(3) + "/" +
                    bgRequestedSink.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE BG AFTER S A/R: " +
                    bgAfterSource.toFixed(3) + "/" +
                    bgAfterSourceResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE BG AFTER T A/R: " +
                    bgAfterSink.toFixed(3) + "/" +
                    bgAfterSinkResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE BG ASSIGNED S/T: " +
                    bgAssignedSource.toFixed(3) + "/" +
                    bgAssignedSink.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG BEFORE S A/R: " +
                    fgBeforeSource.toFixed(3) + "/" +
                    fgBeforeSourceResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG BEFORE T A/R: " +
                    fgBeforeSink.toFixed(3) + "/" +
                    fgBeforeSinkResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG REQUESTED S/T: " +
                    fgRequestedSource.toFixed(3) + "/" +
                    fgRequestedSink.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG AFTER S A/R: " +
                    fgAfterSource.toFixed(3) + "/" +
                    fgAfterSourceResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG AFTER T A/R: " +
                    fgAfterSink.toFixed(3) + "/" +
                    fgAfterSinkResidual.toFixed(3) + "\n"
                );

                output.appendText(
                    "MODE FG ASSIGNED S/T: " +
                    fgAssignedSource.toFixed(3) + "/" +
                    fgAssignedSink.toFixed(3) + "\n"
                );

                // -------------------------------------------------
                var horizontalCaps:ByteArray =
                    qsContext.call(
                        "getDiagnosticHorizontalCapacities"
                    ) as ByteArray;

                if (horizontalCaps != null)
                {
                    horizontalCaps.endian = Endian.LITTLE_ENDIAN;
                    horizontalCaps.position = 0;

                    output.appendText(
                        "HORIZONTAL N-LINKS (" +
                        horizontalCaps.length +
                        " bytes):\n"
                    );

                    for (y = 0; y < height; ++y)
                    {
                        var horizontalRow:String = "";

                        for (x = 0; x + 1 < width; ++x)
                        {
                            var horizontalCap:Number =
                                horizontalCaps.readFloat();

                            horizontalRow +=
                                horizontalCap.toFixed(3);

                            if (x + 2 < width)
                                horizontalRow += " ";
                        }

                        output.appendText(
                            horizontalRow + "\n"
                        );
                    }
                }
                else
                {
                    output.appendText(
                        "HORIZONTAL N-LINKS: null\n"
                    );
                }

                output.appendText("MASK 10x6:\n" + compact);
                output.appendText("HEX 10x6:\n" + bytes);
            }
        }
    }
}
