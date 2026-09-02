#pragma once

#include <cstddef>
#include <cstdint>
#include <vector>

namespace pstouch::quickselection {

class GraphCut {
public:
    using Byte = std::uint8_t;

    // Exact Photoshop Touch QuickSelect 4-neighbor n-link energy.
    static double adobeNeighborWeight(double rgbDistanceSquared);

    // Produces the binary Quick Selection mask:
    //
    //   foreground / terminal 1 -> 0xFF
    //   background / terminal 0 -> 0x00
    //
    // This preserves the polarity recovered from
    // TiledImageGraph::ReadSegmentationMask<unsigned char>().
    static void segment(
        const Byte* imageRGB,
        int width,
        int height,
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints,
        std::vector<Byte>& outputMask
    );
};

} // namespace pstouch::quickselection
