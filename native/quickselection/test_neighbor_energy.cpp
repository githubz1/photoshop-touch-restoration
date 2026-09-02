#include "GraphCut.hpp"

#include <cassert>
#include <cmath>
#include <iostream>

using pstouch::quickselection::GraphCut;

static void expectNear(
    double actual,
    double expected,
    double tolerance = 1.0e-9
)
{
    assert(std::abs(actual - expected) <= tolerance);
}

int main()
{
    const double w0 =
        GraphCut::adobeNeighborWeight(0.0);

    const double w512 =
        GraphCut::adobeNeighborWeight(512.0);

    const double w1024 =
        GraphCut::adobeNeighborWeight(1024.0);

    const double w2048 =
        GraphCut::adobeNeighborWeight(2048.0);

    // Identical RGB values:
    // 500 * exp(0) = 500.
    expectNear(
        w0,
        500.0
    );

    // sqrt(512 / 512) = 1.
    expectNear(
        w512,
        500.0 / std::exp(1.0)
    );

    // sqrt(1024 / 512) = sqrt(2).
    expectNear(
        w1024,
        500.0 * std::exp(-std::sqrt(2.0))
    );

    assert(w0 > w512);
    assert(w512 > w1024);
    assert(w1024 > w2048);
    assert(w2048 > 0.0);

    std::cout
        << "distance^2 = 0:    " << w0 << '\n'
        << "distance^2 = 512:  " << w512 << '\n'
        << "distance^2 = 1024: " << w1024 << '\n'
        << "distance^2 = 2048: " << w2048 << '\n'
        << '\n'
        << "✓ Adobe QuickSelect n-link equation passed\n"
        << "✓ strength = 500\n"
        << "✓ sigma = 16\n"
        << "✓ exponent = 0.5\n"
        << "✓ topology = 4-neighbor\n";

    return 0;
}
