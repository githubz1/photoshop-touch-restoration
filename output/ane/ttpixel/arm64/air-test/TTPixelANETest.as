package
{
    import flash.display.Sprite;
    import flash.text.TextField;
    import flash.text.TextFormat;
    import flash.text.TextFieldAutoSize;
    import flash.external.ExtensionContext;
    import flash.utils.ByteArray;
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
                "_sans",
                40,
                0x000000
            );
            output.autoSize = TextFieldAutoSize.LEFT;
            output.multiline = true;
            output.wordWrap = false;
            output.x = 30;
            output.y = 80;

            addChild(output);

            output.text = "TTPixel ANE Load Test\n\n";
            output.appendText("AIR started: PASS\n");

            var qsClass:Class = TTQuickSelection;
            output.appendText("CLASS: " + (qsClass != null) + "\n");

            var qsContext:ExtensionContext = TTQuickSelection.getExtensionContext();
            output.appendText("CTX: " + (qsContext != null) + "\n");
            var freResult:Boolean = TTQuickSelection.disposeQuickSelectTool();
            output.appendText("FRE: " + freResult + "\n");

            var pixels:ByteArray = new ByteArray();
            pixels.writeByte(0xFF);
            pixels.writeByte(0x40);
            pixels.writeByte(0x80);
            pixels.writeByte(0xC0);
            pixels.position = 0;

            var createResult:Boolean =
                TTQuickSelection.createQuickSelectTool(pixels, 1, 1);

            output.appendText("CREATE: " + createResult + "\n");

            var brushResult:ByteArray =
                TTQuickSelection.brushHit(0, 0, 0, 0, true);

            output.appendText("BRUSH: " + (brushResult != null) + "\n");
            output.appendText("MASKLEN: " + (brushResult != null ? brushResult.length : -1) + "\n");
        }
    }
}
