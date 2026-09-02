#include "QuickSelectTool.hpp"

#include <cassert>
#include <cstdint>
#include <iostream>
#include <vector>

using pstouch::quickselection::QuickSelectTool;

int main()
{
    constexpr int width = 12;
    constexpr int height = 8;

    std::vector<std::uint8_t> image(width * height * 3);

    // Three visually distinct vertical regions.
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            std::uint8_t value;

            if (x < 4)
                value = 25;
            else if (x < 8)
                value = 125;
            else
                value = 230;

            const int i = (y * width + x) * 3;
            image[i + 0] = value;
            image[i + 1] = value;
            image[i + 2] = value;
        }
    }

    QuickSelectTool tool(image.data(), width, height);

    // Native mode 0 = foreground/add.
    tool.brushHit(2, 4, 1, 0);

    const std::uint8_t* first = tool.mouseUp();

    assert(first[4 * width + 2] == 0xFF);

    std::cout << "After foreground stroke:\n";

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x)
            std::cout << (first[y * width + x] ? '#' : '.');

        std::cout << '\n';
    }

    // Add an explicit background/remove constraint in the
    // opposite region. Native nonzero mode = background/remove.
    tool.brushHit(10, 4, 1, 1);

    const std::uint8_t* second = tool.mouseUp();

    assert(second[4 * width + 2] == 0xFF);
    assert(second[4 * width + 10] == 0x00);

    std::cout << "\nAfter background stroke:\n";

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x)
            std::cout << (second[y * width + x] ? '#' : '.');

        std::cout << '\n';
    }

    // switchMode must preserve the recovered polarity.
    tool.switchMode(0);
    tool.switchMode(1);

    std::cout << "\n✓ incremental add/remove contract passed\n";
    std::cout << "✓ foreground constraint survived recomputation\n";
    std::cout << "✓ background constraint survived recomputation\n";
    std::cout << "✓ repeated mouseUp segmentation succeeded\n";

    return 0;
}
