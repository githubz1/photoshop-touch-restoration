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
            const int i = (y * width + x) * 3;

            image[i + 0] = value;
            image[i + 1] = value;
            image[i + 2] = value;
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

    return 0;
}
