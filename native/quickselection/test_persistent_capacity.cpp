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

    std::cout << "✓ Adobe positive capacity delta updates residual\n";
    std::cout << "✓ Adobe negative capacity delta clamps residual at zero\n";
    std::cout << "✓ Adobe persistent terminal delta primitive passed\n";

    return EXIT_SUCCESS;
}
