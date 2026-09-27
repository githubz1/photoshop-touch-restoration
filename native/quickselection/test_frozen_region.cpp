#include "GraphCut.hpp"

#include <cstdlib>
#include <iostream>
#include <vector>

using pstouch::quickselection::GraphCut;

int main()
{
    constexpr int width = 5;
    constexpr int height = 5;

    // Uniform RGB image keeps the graph deterministic.
    std::vector<unsigned char> image(width * height * 3, 128);
    std::vector<unsigned char> foreground(width * height, 0);
    std::vector<unsigned char> background(width * height, 0);
    std::vector<unsigned char> output;

    GraphCut graph;

    // Establish the original graph with one hard foreground constraint.
    foreground[0] = 0xff;

    graph.initialize(
        image.data(),
        width,
        height,
        foreground.data(),
        background.data()
    );

    graph.calculateMinimumCut(output);

    // Clear the original constraint. Adobe SetFrozenEnergies skips
    // unconstrained pixels, so the existing terminal state must survive.
    foreground[0] = 0x00;

    // Exercise Adobe's non-zero-origin rectangle convention.
    //
    // QuickSelection_BrushHit advances both mask pointers to the
    // rectangle's top-left before calling SetFrozenEnergies. The callee
    // then walks those pointers using the full-image row stride while
    // graph nodes remain addressed in absolute image coordinates.
    constexpr int regionX = 2;
    constexpr int regionY = 2;
    constexpr int regionWidth = 2;
    constexpr int regionHeight = 2;

    const std::size_t regionOffset =
        static_cast<std::size_t>(regionY) *
            static_cast<std::size_t>(width) +
        static_cast<std::size_t>(regionX);

    // Hard-freeze absolute pixel (3,3) as background. Relative to the
    // offset mask pointer this is row 1, column 1.
    background[3 * width + 3] = 0xff;

    graph.updateFrozenEnergies(
        foreground.data() + regionOffset,
        background.data() + regionOffset,
        regionX,
        regionY,
        regionWidth,
        regionHeight
    );

    graph.calculateMinimumCut(output);

    if (output.size() != width * height) {
        std::cerr << "FAIL: unexpected output size\n";
        return EXIT_FAILURE;
    }

    if (output[0] != 0xff) {
        std::cerr
            << "FAIL: unconstrained pixel inside update rectangle "
            << "was reset instead of skipped\n";
        return EXIT_FAILURE;
    }

    if (output[3 * width + 3] != 0x00) {
        std::cerr
            << "FAIL: non-zero-origin background constraint "
            << "did not address absolute pixel (3,3)\n";
        return EXIT_FAILURE;
    }

    std::cout << "✓ previous frozen terminal state survived\n";
    std::cout << "✓ non-zero-origin rectangle addressed pixel (3,3)\n";
    std::cout << "✓ Adobe SetFrozenEnergies pointer-offset semantics passed\n";

    return EXIT_SUCCESS;
}
