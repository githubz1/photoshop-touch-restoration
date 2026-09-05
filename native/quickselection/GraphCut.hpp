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

    // Build the persistent ImageGraph baseline.
    //
    // Adobe equivalent:
    //   Create / BuildNodesSegmentation
    //   SetLocalEnergies
    //   SetNeighborEnergiesRGBType
    void initialize(
        const Byte* imageRGB,
        int width,
        int height,
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints
    );

    // Update frozen terminal energies only inside the supplied region.
    //
    // Adobe equivalent:
    //   ImageGraph::SetFrozenEnergies<unsigned char>()
    void updateFrozenEnergies(
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints,
        int x,
        int y,
        int width,
        int height
    );

    // Run one minimum cut on the retained graph and produce the
    // binary Quick Selection mask.
    //
    // Adobe equivalent:
    //   mincut_adjlist_graph<float>::CalculateMinimumCut()
    void calculateMinimumCut(
        std::vector<Byte>& outputMask
    );

    // Compatibility wrapper used by the existing tests/callers.
    // First call initializes the graph. Later calls update the full
    // image region, then perform one minimum cut.
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
