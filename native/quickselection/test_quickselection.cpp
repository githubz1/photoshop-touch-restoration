#include "QuickSelectTool.hpp"

#include <cassert>
#include <cstdint>
#include <iostream>
#include <vector>

using pstouch::quickselection::QuickSelectTool;

int main()
{
    constexpr int width = 10;
    constexpr int height = 6;

    std::vector<std::uint8_t> image(width * height * 3);

    // Left half = dark, right half = bright.
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const std::uint8_t value = x < 5 ? 20 : 235;
            const int pixel = y * width + x;
            const int pixelCount = width * height;

            // ARMv7-verified QuickSelectTool input layout:
            // R plane, followed by G plane, followed by B plane.
            image[pixel] = value;
            image[pixelCount + pixel] = value;
            image[pixelCount * 2 + pixel] = value;
        }
    }

    QuickSelectTool tool(image.data(), width, height);

    // Recovered native polarity:
    // mode 0 = foreground/add
    // mode 1 = background/remove
    tool.brushHit(2, 3, 1, 0);
    tool.brushHit(7, 3, 1, 1);

    const std::uint8_t* mask = tool.mouseUp();

    std::cout << "Result mask:\n";

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const auto value = mask[y * width + x];
            std::cout << (value == 0xFF ? '#' : '.');
        }
        std::cout << '\n';
    }

    // Scribbled foreground must remain foreground.
    assert(mask[3 * width + 2] == 0xFF);

    // Scribbled background must remain background.
    assert(mask[3 * width + 7] == 0x00);

    // With the strong image boundary, the expected segmentation is:
    // dark left half foreground, bright right half background.
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const auto expected = x < 5 ? 0xFF : 0x00;
            assert(mask[y * width + x] == expected);
        }
    }

    std::cout << "\n✓ Quick Selection behavioral test passed\n";
    std::cout << "✓ mode 0 -> foreground -> FF\n";
    std::cout << "✓ nonzero -> background -> 00\n";

    {
        constexpr int pyramidTestWidth = 4097;
        constexpr int pyramidTestHeight = 1024;

        const std::size_t pyramidTestPixels =
            static_cast<std::size_t>(pyramidTestWidth) *
            static_cast<std::size_t>(pyramidTestHeight);

        std::vector<std::uint8_t> pyramidInput(
            pyramidTestPixels * 3,
            0
        );

        // Give each 2x2 source block a known integer-average result.
        //
        // First output pixel:
        //   10 20
        //   30 40
        // average = 25
        pyramidInput[0] = 10;
        pyramidInput[1] = 20;
        pyramidInput[pyramidTestWidth] = 30;
        pyramidInput[pyramidTestWidth + 1] = 40;

        // Same coordinates in G and B prove the planar offsets too.
        pyramidInput[pyramidTestPixels + 0] = 50;
        pyramidInput[pyramidTestPixels + 1] = 60;
        pyramidInput[pyramidTestPixels + pyramidTestWidth] = 70;
        pyramidInput[pyramidTestPixels + pyramidTestWidth + 1] = 80;

        pyramidInput[pyramidTestPixels * 2 + 0] = 90;
        pyramidInput[pyramidTestPixels * 2 + 1] = 100;
        pyramidInput[pyramidTestPixels * 2 + pyramidTestWidth] = 110;
        pyramidInput[pyramidTestPixels * 2 + pyramidTestWidth + 1] = 120;

        QuickSelectTool pyramidTool(
            pyramidInput.data(),
            pyramidTestWidth,
            pyramidTestHeight
        );

        assert(pyramidTool.pyramidResizeRequired());
        assert(pyramidTool.pyramidLevel() == 1);
        assert(pyramidTool.pyramidWidth() == 2048);
        assert(pyramidTool.pyramidHeight() == 512);

        assert(pyramidTool.diagnosticPyramidRGB(0, 0, 0) == 25);
        assert(pyramidTool.diagnosticPyramidRGB(1, 0, 0) == 65);
        assert(pyramidTool.diagnosticPyramidRGB(2, 0, 0) == 105);

        std::cout << "✓ Adobe pyramid threshold/halving passed\n";
        std::cout << "✓ Adobe 2x2 integer box-average passed\n";
        std::cout << "✓ planar RGB pyramid layout passed\n";
    }

    return 0;
}
