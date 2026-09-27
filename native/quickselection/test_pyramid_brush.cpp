#include "QuickSelectTool.hpp"

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <iostream>
#include <vector>

int main()
{
    constexpr int width = 4097;
    constexpr int height = 1024;

    const std::size_t pixels =
        static_cast<std::size_t>(width) *
        static_cast<std::size_t>(height);

    std::vector<std::uint8_t> planarRGB(pixels * 3, 0);

    // Strong vertical two-region image.
    // Left half dark, right half bright.
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const std::size_t i =
                static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(width) +
                static_cast<std::size_t>(x);

            const std::uint8_t value =
                x < width / 2 ? 20 : 235;

            planarRGB[i] = value;
            planarRGB[pixels + i] = value;
            planarRGB[pixels * 2 + i] = value;
        }
    }

    pstouch::quickselection::QuickSelectTool tool(
        planarRGB.data(),
        width,
        height
    );

    if (!tool.pyramidResizeRequired()) {
        std::cerr << "FAIL: pyramid was not activated\n";
        return 1;
    }

    if (
        tool.pyramidLevel() != 1 ||
        tool.pyramidWidth() != 2048 ||
        tool.pyramidHeight() != 512
    ) {
        std::cerr
            << "FAIL: unexpected pyramid geometry: level="
            << tool.pyramidLevel()
            << " size="
            << tool.pyramidWidth()
            << "x"
            << tool.pyramidHeight()
            << "\n";
        return 1;
    }

    // Original-resolution brush coordinates.
    //
    // Adobe level 1 mapping:
    //   FG: (1024,512,r=8) -> (512,256,r=4)
    //   BG: (3072,512,r=8) -> (1536,256,r=4)
    const int fgX = 1024;
    const int fgY = 512;
    const int fgR = 8;

    const int bgX = 3072;
    const int bgY = 512;
    const int bgR = 8;

    tool.qsBrushHits(
        &fgX,
        &fgY,
        &fgR,
        0,
        1
    );

    tool.qsBrushHits(
        &bgX,
        &bgY,
        &bgR,
        1,
        1
    );

    const std::uint8_t* mask = tool.getMask();

    if (mask == nullptr) {
        std::cerr << "FAIL: getMask() returned null\n";
        return 1;
    }

    std::size_t selected = 0;

    for (std::size_t i = 0; i < pixels; ++i) {
        if (mask[i] != 0) {
            ++selected;
        }
    }

    const auto sample = [&](int x, int y) -> int {
        return static_cast<int>(
            mask[
                static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(width) +
                static_cast<std::size_t>(x)
            ]
        );
    };

    std::cout
        << "PYRAMID: "
        << tool.pyramidWidth()
        << "x"
        << tool.pyramidHeight()
        << " level="
        << tool.pyramidLevel()
        << "\n";

    std::cout
        << "SELECTED: "
        << selected
        << " / "
        << pixels
        << "\n";

    std::cout
        << "SAMPLES:"
        << " left=" << sample(1024, 512)
        << " edgeL=" << sample(2046, 512)
        << " edgeR=" << sample(2050, 512)
        << " right=" << sample(3072, 512)
        << "\n";

    std::cout
        << "FRESH CUT MISMATCHES: "
        << tool.freshCutMismatchCount()
        << "\n";

    std::cout
        << "REFINEMENT PRE-READBACK MISMATCHES: "
        << tool.refinementPreReadbackMismatchCount()
        << "\n";

    if (sample(1024, 512) == 0) {
        std::cerr << "FAIL: foreground side is not selected\n";
        return 1;
    }

    if (sample(3072, 512) != 0) {
        std::cerr << "FAIL: background side is selected\n";
        return 1;
    }

    if (tool.freshCutMismatchCount() != -1) {
        std::cerr
            << "FAIL: fresh-cut oracle unexpectedly ran "
               "outside the 10x6 diagnostic fixture\n";
        return 1;
    }

    std::cout
        << "PASS: resized qsBrushHits -> refinement -> "
           "ResizeMask path completed\n";

    const std::uint8_t* mouseUpMask = tool.mouseUp();

    if (mouseUpMask == nullptr) {
        std::cerr << "FAIL: mouseUp() returned null\n";
        return 1;
    }

    std::size_t mouseUpSelected = 0;

    for (std::size_t i = 0; i < pixels; ++i) {
        if (mouseUpMask[i] != 0) {
            ++mouseUpSelected;
        }
    }

    const auto mouseUpSample = [&](int x, int y) -> int {
        return static_cast<int>(
            mouseUpMask[
                static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(width) +
                static_cast<std::size_t>(x)
            ]
        );
    };

    std::cout
        << "MOUSEUP SELECTED: "
        << mouseUpSelected
        << " / "
        << pixels
        << "\n";

    std::cout
        << "MOUSEUP GRAPH:"
        << " tiles=" << tool.mouseUpActiveTileCount()
        << " nodes=" << tool.mouseUpBuiltNodeCount()
        << " links=" << tool.mouseUpNeighborLinkCount()
        << "\n";

    if (tool.mouseUpActiveTileCount() <= 0 ||
        tool.mouseUpBuiltNodeCount() <= 0 ||
        tool.mouseUpNeighborLinkCount() <= 0) {
        std::cerr
            << "FAIL: MouseUp final-resolution graph was not built\n";
        return 1;
    }

    std::cout
        << "MOUSEUP SAMPLES:"
        << " left=" << mouseUpSample(1024, 512)
        << " edgeL=" << mouseUpSample(2046, 512)
        << " edgeR=" << mouseUpSample(2050, 512)
        << " right=" << mouseUpSample(3072, 512)
        << "\n";

    if (mouseUpSample(1024, 512) == 0) {
        std::cerr
            << "FAIL: MouseUp lost foreground constraint\n";
        return 1;
    }

    if (mouseUpSample(3072, 512) != 0) {
        std::cerr
            << "FAIL: MouseUp selected background constraint\n";
        return 1;
    }

    std::cout
        << "PASS: resized MouseUp final-resolution "
           "RefineEdges path completed\n";

    return 0;
}
