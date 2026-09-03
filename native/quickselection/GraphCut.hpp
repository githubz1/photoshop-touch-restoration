#pragma once

#include <cstddef>
#include <cstdint>
#include <memory>
#include <vector>

namespace pstouch::quickselection {

class GraphCut {
public:
    GraphCut();
    ~GraphCut();

    GraphCut(const GraphCut&) = delete;
    GraphCut& operator=(const GraphCut&) = delete;

    GraphCut(GraphCut&&) noexcept;
    GraphCut& operator=(GraphCut&&) noexcept;

    void reset();

    static bool testPersistentCapacityUpdate();

    using Byte = std::uint8_t;

    // Exact Photoshop Touch QuickSelect 4-neighbor n-link energy.
    static double adobeNeighborWeight(double rgbDistanceSquared);

    struct TerminalWeights {
        double terminal0;
        double terminal1;
    };

    // Exact active QuickSelect SetLocalEnergies param_7 == 1 path.
    static TerminalWeights adobeTerminalWeights(
        bool foregroundConstraint,
        bool backgroundConstraint
    );

    // Produces the binary Quick Selection mask:
    //
    //   foreground / terminal 1 -> 0xFF
    //   background / terminal 0 -> 0x00
    //
    // This preserves the polarity recovered from
    // TiledImageGraph::ReadSegmentationMask<unsigned char>().
    void segment(
        const Byte* imageRGB,
        int width,
        int height,
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints,
        std::vector<Byte>& outputMask
    );
private:
    struct Impl;
    std::unique_ptr<Impl> impl_;

};

} // namespace pstouch::quickselection
