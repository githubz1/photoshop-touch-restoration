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

    // Clear the mask byte, but deliberately include pixel 0 in an update
    // rectangle containing no frozen pixels.
    //
    // Adobe SetFrozenEnergies skips this pixel entirely. Its existing
    // terminal state must therefore survive.
    foreground[0] = 0x00;

    graph.updateFrozenEnergies(
        foreground.data(),
        background.data(),
        0,
        0,
        2,
        2
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

    std::cout << "✓ unconstrained update-region pixel was skipped\n";
    std::cout << "✓ previous frozen terminal state survived\n";
    std::cout << "✓ Adobe SetFrozenEnergies rectangle semantics passed\n";

    return EXIT_SUCCESS;
}
