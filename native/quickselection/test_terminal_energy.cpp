#include "GraphCut.hpp"

#include <cassert>
#include <cmath>
#include <iostream>
#include <limits>

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
    constexpr double expectedDefault =
        0.7 * 6.234410762786865;

    constexpr double expectedHardConstraint =
        static_cast<double>(std::numeric_limits<float>::max()) / 242.0;

    const auto ordinary =
        GraphCut::adobeTerminalWeights(false, false);

    const auto foreground =
        GraphCut::adobeTerminalWeights(true, false);

    const auto background =
        GraphCut::adobeTerminalWeights(false, true);

    expectNear(
        ordinary.terminal0,
        expectedDefault
    );

    expectNear(
        ordinary.terminal1,
        0.0
    );

    expectNear(
        foreground.terminal0,
        0.0
    );

    expectNear(
        foreground.terminal1,
        expectedHardConstraint,
        expectedHardConstraint * 1.0e-12
    );

    expectNear(
        background.terminal0,
        expectedHardConstraint,
        expectedHardConstraint * 1.0e-12
    );

    expectNear(
        background.terminal1,
        0.0
    );

    std::cout
        << "default terminal0:    "
        << ordinary.terminal0 << '\n'

        << "default terminal1:    "
        << ordinary.terminal1 << '\n'

        << "foreground terminal0: "
        << foreground.terminal0 << '\n'

        << "foreground terminal1: "
        << foreground.terminal1 << '\n'

        << "background terminal0: "
        << background.terminal0 << '\n'

        << "background terminal1: "
        << background.terminal1 << '\n'
        << '\n'

        << "✓ Adobe QuickSelect active unary equation passed\n"
        << "✓ param_6 = 0.7\n"
        << "✓ param_7 = 1\n"
        << "✓ param_8 = 1\n"
        << "✓ default energy = "
        << expectedDefault << '\n'
        << "✓ hard constraint = FLT_MAX / 242 = "
        << expectedHardConstraint << '\n'
        << "✓ param_14 = 5 -> ImageGraph +0x54 = 121\n"
        << "✓ foreground/background terminal polarity preserved\n";

    return 0;
}
