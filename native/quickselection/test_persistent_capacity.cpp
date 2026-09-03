#include "GraphCut.hpp"

#include <cstdlib>
#include <iostream>

using pstouch::quickselection::GraphCut;

int main()
{
    if (!GraphCut::testPersistentCapacityUpdate()) {
        std::cerr << "FAIL: persistent capacity update\n";
        return EXIT_FAILURE;
    }

    std::cout << "✓ capacity increase preserves existing flow\n";
    std::cout << "✓ capacity decrease clamps flow to new capacity\n";
    std::cout << "✓ persistent capacity primitive passed\n";

    return EXIT_SUCCESS;
}
