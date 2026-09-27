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
        bool backgroundConstraint,
        bool foregroundMode
    );

    // Exact tiled-refinement BuildNodesAndSetLocalEnergiesInMask
    // terminal weights. Tiled ImageGraph::Construct initializes
    // ImageGraph + 0x54 to 1, so Adobe's hard constraint is:
    //
    //   FLT_MAX / (2 * 1)
    //
    static TerminalWeights adobeRefinementTerminalWeights(
        bool foregroundConstraint,
        bool backgroundConstraint,
        bool foregroundMode
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

    // Recalculate full-image local terminal energies after a
    // Quick Selection mode change.
    //
    // Adobe equivalent:
    //   ImageGraph::SetLocalEnergies<unsigned char, unsigned char>()
    void switchMode(
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints,
        bool foregroundMode
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

    // Temporary TTPixel diagnostic.
    // Returns the number of source/sink classification differences
    // between the persistent Adobe BK result and the independent
    // fresh FlowGraph result. -1 means no comparison has been run.
    int freshCutMismatchCount() const noexcept;

    // Temporary TTPixel diagnostic.
    // Final assigned SOURCE -> pixel terminal capacities.
    const std::vector<float>& diagnosticSourceCapacities() const noexcept;

    // Temporary TTPixel diagnostic.
    // Final assigned pixel -> SINK terminal capacities.
    const std::vector<float>& diagnosticSinkCapacities() const noexcept;

    // Temporary TTPixel diagnostic.
    // Capacities most recently REQUESTED by GraphCut::switchMode()
    // before AdobeBKGraph::updateTweights() processes them.
    const std::vector<float>& diagnosticRequestedSourceCapacities() const noexcept;
    const std::vector<float>& diagnosticRequestedSinkCapacities() const noexcept;

    const std::vector<float>& diagnosticBeforeUpdateSourceCapacities() const noexcept;
    const std::vector<float>& diagnosticBeforeUpdateSinkCapacities() const noexcept;
    const std::vector<float>& diagnosticAfterUpdateSourceCapacities() const noexcept;
    const std::vector<float>& diagnosticAfterUpdateSinkCapacities() const noexcept;

    const std::vector<float>& diagnosticBeforeUpdateSourceResiduals() const noexcept;
    const std::vector<float>& diagnosticBeforeUpdateSinkResiduals() const noexcept;
    const std::vector<float>& diagnosticAfterUpdateSourceResiduals() const noexcept;
    const std::vector<float>& diagnosticAfterUpdateSinkResiduals() const noexcept;

    // Temporary TTPixel diagnostic.
    // Assigned left-to-right horizontal n-link capacities.
    const std::vector<float>& diagnosticHorizontalCapacities() const noexcept;

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

// Temporary diagnostic for the recovered Adobe BK solver self-test.
// The self-test runs during native static initialization.
bool adobeBKSelfTestPassed() noexcept;
const char* adobeBKSelfTestFailure() noexcept;

} // namespace pstouch::quickselection
