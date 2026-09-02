#include "QuickSelectTool.hpp"

#include <cassert>
#include <cstddef>
#include <iostream>
#include <vector>

using pstouch::quickselection::QuickSelectTool;
using Byte = QuickSelectTool::Byte;

static void printMask(
    const Byte* mask,
    int width,
    int height
)
{
    assert(mask != nullptr);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const std::size_t index =
                static_cast<std::size_t>(y * width + x);

            std::cout << (mask[index] ? '#' : '.');
        }

        std::cout << '\n';
    }
}

static void assertMaskIsClear(
    const Byte* mask,
    std::size_t pixelCount
)
{
    assert(mask != nullptr);

    for (std::size_t i = 0; i < pixelCount; ++i) {
        assert(mask[i] == 0);
    }
}

int main()
{
    constexpr int width = 8;
    constexpr int height = 4;

    constexpr std::size_t pixels =
        static_cast<std::size_t>(width * height);

    std::vector<Byte> imageRGB(pixels * 3, 128);

    QuickSelectTool tool(
        imageRGB.data(),
        width,
        height
    );

    std::vector<Byte> suppliedMask(pixels, 0);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width / 2; ++x) {
            suppliedMask[
                static_cast<std::size_t>(y * width + x)
            ] = 0xff;
        }
    }

    tool.setCurrentMask(
        suppliedMask.data(),
        suppliedMask.size()
    );

    const Byte* immediatelyAfter =
        tool.getMask();

    std::cout
        << "Mask immediately after setCurrentMask():\n";

    printMask(
        immediatelyAfter,
        width,
        height
    );

    assertMaskIsClear(
        immediatelyAfter,
        pixels
    );

    const Byte* afterMouseUp =
        tool.mouseUp();

    std::cout
        << "\nMask after mouseUp():\n";

    printMask(
        afterMouseUp,
        width,
        height
    );

    assertMaskIsClear(
        afterMouseUp,
        pixels
    );

    std::cout
        << "\n"
        << "✓ qsSetCurrMask reset contract passed\n"
        << "✓ supplied mask is not treated as a soft prior\n"
        << "✓ param_7 == 1 behavior preserved\n";

    return 0;
}
