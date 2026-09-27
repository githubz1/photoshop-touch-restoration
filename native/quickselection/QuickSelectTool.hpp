#pragma once

#include <cstddef>
#include <cstdint>
#include <vector>

#include "GraphCut.hpp"
#include "TiledRefinement.hpp"

namespace pstouch::quickselection {

class QuickSelectTool {
public:
    using Byte = std::uint8_t;

    QuickSelectTool(const Byte* imageRGB, int width, int height);
    ~QuickSelectTool() = default;

    QuickSelectTool(const QuickSelectTool&) = delete;
    QuickSelectTool& operator=(const QuickSelectTool&) = delete;

    // Recovered QuickSelectTool behavioral surface.
    void clearMasks();

    void setCurrentMask(const Byte* mask, std::size_t size);

    void brushHit(
        int x,
        int y,
        int radius,
        int mode
    );

    void brushHits(
        const int* x,
        const int* y,
        const int* radius,
        const int* mode,
        int count
    );

    void qsBrushHits(
        const int* x,
        const int* y,
        const int* radius,
        int mode,
        int count
    );

    void switchMode(int mode);
    void reset(int mode);

    const Byte* getMask() const noexcept;

    // Temporary TTPixel diagnostic.
    int freshCutMismatchCount() const noexcept;
    int refinementChangedPixelCount() const noexcept;
    int refinementPreReadbackMismatchCount() const noexcept;
    int refinementActiveTileCount() const noexcept;
    int refinementBuiltNodeCount() const noexcept;
    int refinementForcedForegroundNodeCount() const noexcept;
    int refinementForcedBackgroundNodeCount() const noexcept;
    int refinementFrozenForegroundNodeCount() const noexcept;
    int refinementFrozenBackgroundNodeCount() const noexcept;
    int refinementUniqueFrozenNodeCount() const noexcept;
    int refinementOverlappingFrozenNodeCount() const noexcept;
    int refinementNeighborLinkCount() const noexcept;

    int mouseUpActiveTileCount() const noexcept;
    int mouseUpBuiltNodeCount() const noexcept;
    int mouseUpNeighborLinkCount() const noexcept;

    const std::vector<float>&
    diagnosticSourceCapacities() const noexcept;

    const std::vector<float>&
    diagnosticSinkCapacities() const noexcept;

    const std::vector<float>&
    diagnosticRequestedSourceCapacities() const noexcept;

    const std::vector<float>&
    diagnosticRequestedSinkCapacities() const noexcept;

    const std::vector<float>&
    diagnosticBeforeUpdateSourceCapacities() const noexcept;

    const std::vector<float>&
    diagnosticBeforeUpdateSinkCapacities() const noexcept;

    const std::vector<float>&
    diagnosticAfterUpdateSourceCapacities() const noexcept;

    const std::vector<float>&
    diagnosticAfterUpdateSinkCapacities() const noexcept;

    const std::vector<float>&
    diagnosticBeforeUpdateSourceResiduals() const noexcept;

    const std::vector<float>&
    diagnosticBeforeUpdateSinkResiduals() const noexcept;

    const std::vector<float>&
    diagnosticAfterUpdateSourceResiduals() const noexcept;

    const std::vector<float>&
    diagnosticAfterUpdateSinkResiduals() const noexcept;

    const std::vector<float>&
    diagnosticHorizontalCapacities() const noexcept;

    const Byte* mouseUp();

    int width() const noexcept { return width_; }
    int height() const noexcept { return height_; }

    // Read-only diagnostics for the recovered Adobe pyramid setup.
    int pyramidWidth() const noexcept { return pyramidWidth_; }
    int pyramidHeight() const noexcept { return pyramidHeight_; }
    int pyramidLevel() const noexcept { return pyramidLevel_; }
    bool pyramidResizeRequired() const noexcept
    {
        return pyramidResizeRequired_;
    }

    Byte diagnosticPyramidRGB(
        int channel,
        int x,
        int y
    ) const noexcept;

private:
    struct BrushRegion {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
    };

    void ensureGraphInitialized();
    void refineEdgesSegmentation();
    void refineEdges(int borderRadius);

    BrushRegion stampBrushHit(
        int x,
        int y,
        int radius,
        int mode
    );

    static bool isForegroundMode(int mode) noexcept
    {
        // Proven from the ARMv7 implementation:
        //
        // mode == 0  -> foreground/add
        // mode != 0  -> background/remove
        return mode == 0;
    }

    std::size_t pixelCount() const noexcept;

    // Adobe QuickSelectTool keeps both the original-resolution image
    // and, when required, a lower-resolution working pyramid image.
    //
    // GetHigherPyramidLevel() repeatedly halves both dimensions until
    // width * height <= 0x400000.
    static constexpr std::size_t kMaximumPyramidPixels = 0x400000u;

    int width_ = 0;
    int height_ = 0;

    int pyramidWidth_ = 0;
    int pyramidHeight_ = 0;
    int pyramidLevel_ = 0;
    bool pyramidResizeRequired_ = false;

    // Original full-resolution three-plane RGB input.
    std::vector<Byte> imageRGB_;

    // Adobe +0x70: working three-plane RGB image.  At pyramid level
    // zero this is an exact copy.  At higher levels each output sample
    // is the integer average of a 2^level x 2^level source block.
    std::vector<Byte> pyramidImageRGB_;

    // Recovered semantic equivalents of the important native buffers.
    //
    // ARMv7 +0x68 -> final selection mask
    // ARMv7 +0x74 -> foreground constraint/scribble mask
    // ARMv7 +0x78 -> background constraint/scribble mask
    std::vector<Byte> selectionMask_;

    std::vector<Byte> foregroundConstraints_;
    std::vector<Byte> backgroundConstraints_;

    // Adobe +0x74 / +0x78 / +0x80.
    // Separate working-resolution masks exist only when a pyramid
    // reduction is required. At level zero Adobe aliases the original
    // masks instead.
    std::vector<Byte> pyramidForegroundConstraints_;
    std::vector<Byte> pyramidBackgroundConstraints_;
    std::vector<Byte> pyramidSelectionMask_;

    // Persistent segmentation graph.
    //
    // Adobe's QuickSelectTool owns its ImageGraph for the lifetime of
    // the tool. Brush updates modify that existing graph rather than
    // constructing a new graph for every stroke batch.
    GraphCut graphCut_;
    bool graphInitialized_ = false;

    // Semantic equivalent of ARMv7 QuickSelectTool +0xEC.
    bool foregroundMode_ = true;

    int refinementChangedPixelCount_ = -1;
    int refinementPreReadbackMismatchCount_ = -1;
    int refinementActiveTileCount_ = 0;
    int refinementBuiltNodeCount_ = 0;
    int refinementForcedForegroundNodeCount_ = 0;
    int refinementForcedBackgroundNodeCount_ = 0;
    int refinementFrozenForegroundNodeCount_ = 0;
    int refinementFrozenBackgroundNodeCount_ = 0;
    int refinementUniqueFrozenNodeCount_ = 0;
    int refinementOverlappingFrozenNodeCount_ = 0;
    int refinementNeighborLinkCount_ = 0;

    int mouseUpActiveTileCount_ = 0;
    int mouseUpBuiltNodeCount_ = 0;
    int mouseUpNeighborLinkCount_ = 0;
};

} // namespace pstouch::quickselection
