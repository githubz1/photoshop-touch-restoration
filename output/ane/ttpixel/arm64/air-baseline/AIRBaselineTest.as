package
{
    import flash.display.Sprite;
    import flash.text.TextField;
    import flash.text.TextFormat;
    import flash.text.TextFieldAutoSize;

    public class AIRBaselineTest extends Sprite
    {
        public function AIRBaselineTest()
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

            output.text = "AIR 51.3 ARM64 Baseline\n\n";
            output.appendText("AIR started: PASS\n");
            output.appendText("No TTPixel ANE loaded.\n");
        }
    }
}
