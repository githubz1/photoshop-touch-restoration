#include "QuickSelectTool.hpp"

#include <cstdint>
#include <iostream>
#include <vector>

using pstouch::quickselection::QuickSelectTool;

int main()
{
    constexpr int width = 8;
    constexpr int height = 4;

    // Uniform image deliberately provides no useful image boundary.
    std::vector<std::uint8_t> image(width * height * 3, 128);

    // Existing selection: left half selected.
    std::vector<std::uint8_t> initialMask(width * height, 0);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < 4; ++x) {
            initialMask[y * width + x] = 0xFF;
        }
    }

    QuickSelectTool tool(image.data(), width, height);

    tool.setCurrentMask(initialMask.data(), initialMask.size());

    std::cout << "Mask immediately after setCurrentMask():\n";

    const std::uint8_t* before = tool.getMask();

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x)
            std::cout << (before[y * width + x] ? '#' : '.');

        std::cout << '\n';
    }

    // Force graph-cut recomputation.
    const std::uint8_t* after = tool.mouseUp();

    std::cout << "\nMask after mouseUp():\n";

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x)
            std::cout << (after[y * width + x] ? '#' : '.');

        std::cout << '\n';
    }

    return 0;
}
