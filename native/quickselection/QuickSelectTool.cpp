#include "QuickSelectTool.hpp"
#include "TiledRefinement.hpp"
#include "GraphCut.hpp"

#include <algorithm>
#include <cmath>
#include <stdexcept>

namespace pstouch::quickselection {

QuickSelectTool::QuickSelectTool(
    const Byte* imageRGB,
    int width,
    int height
)
    : width_(width),
      height_(height)
{
    if (width <= 0 || height <= 0) {
        throw std::invalid_argument("QuickSelectTool: invalid dimensions");
    }

    const std::size_t pixels = pixelCount();

    if (imageRGB != nullptr) {
        imageRGB_.assign(imageRGB, imageRGB + pixels * 3);
    } else {
        imageRGB_.assign(pixels * 3, 0);
    }

    selectionMask_.assign(pixels, 0);
    foregroundConstraints_.assign(pixels, 0);
    backgroundConstraints_.assign(pixels, 0);

    // Adobe GetHigherPyramidLevel(0, width, height, 0x400000, ...).
    pyramidWidth_ = width_;
    pyramidHeight_ = height_;
    pyramidLevel_ = 0;

    while (
        static_cast<std::size_t>(pyramidWidth_) *
            static_cast<std::size_t>(pyramidHeight_) >
        kMaximumPyramidPixels
    ) {
        pyramidWidth_ /= 2;
        pyramidHeight_ /= 2;
        ++pyramidLevel_;
    }

    if (pyramidHeight_ < 1) {
        pyramidHeight_ = 1;
    }

    if (pyramidWidth_ < 1) {
        pyramidWidth_ = 1;
    }

    pyramidResizeRequired_ =
        pyramidWidth_ != width_ ||
        pyramidHeight_ != height_;

    const std::size_t pyramidPixels =
        static_cast<std::size_t>(pyramidWidth_) *
        static_cast<std::size_t>(pyramidHeight_);

    pyramidImageRGB_.assign(pyramidPixels * 3, 0);

    if (!pyramidResizeRequired_) {
        // Adobe CopyImage(): exact planar copy at pyramid level zero.
        pyramidImageRGB_ = imageRGB_;
    } else {
        // Adobe DownsizeImage(): each pyramid pixel is the integer
        // average of a complete 2^level x 2^level source block.
        const int scale = 1 << pyramidLevel_;

        for (int channel = 0; channel < 3; ++channel) {
            const std::size_t sourcePlane =
                static_cast<std::size_t>(channel) * pixels;

            const std::size_t destinationPlane =
                static_cast<std::size_t>(channel) * pyramidPixels;

            for (int y = 0; y < pyramidHeight_; ++y) {
                const int sourceY = y * scale;

                for (int x = 0; x < pyramidWidth_; ++x) {
                    const int sourceX = x * scale;

                    std::uint64_t sum = 0;
                    int sampleCount = 0;

                    for (int dy = 0; dy < scale; ++dy) {
                        const std::size_t sourceRow =
                            sourcePlane +
                            static_cast<std::size_t>(sourceY + dy) *
                                static_cast<std::size_t>(width_);

                        for (int dx = 0; dx < scale; ++dx) {
                            sum += imageRGB_[
                                sourceRow +
                                static_cast<std::size_t>(sourceX + dx)
                            ];
                            ++sampleCount;
                        }
                    }

                    pyramidImageRGB_[
                        destinationPlane +
                        static_cast<std::size_t>(y) *
                            static_cast<std::size_t>(pyramidWidth_) +
                        static_cast<std::size_t>(x)
                    ] = static_cast<Byte>(
                        sum / static_cast<std::uint64_t>(sampleCount)
                    );
                }
            }
        }
    }

    // Adobe allocates +0x74/+0x78/+0x80 only when the working
    // dimensions differ from the original dimensions.
    if (pyramidResizeRequired_) {
        pyramidForegroundConstraints_.assign(pyramidPixels, 0);
        pyramidBackgroundConstraints_.assign(pyramidPixels, 0);
        pyramidSelectionMask_.assign(pyramidPixels, 0);
    } else {
        pyramidForegroundConstraints_.clear();
        pyramidBackgroundConstraints_.clear();
        pyramidSelectionMask_.clear();
    }

}

QuickSelectTool::Byte
QuickSelectTool::diagnosticPyramidRGB(
    int channel,
    int x,
    int y
) const noexcept
{
    if (
        channel < 0 || channel >= 3 ||
        x < 0 || x >= pyramidWidth_ ||
        y < 0 || y >= pyramidHeight_
    ) {
        return 0;
    }

    const std::size_t pyramidPixels =
        static_cast<std::size_t>(pyramidWidth_) *
        static_cast<std::size_t>(pyramidHeight_);

    return pyramidImageRGB_[
        static_cast<std::size_t>(channel) * pyramidPixels +
        static_cast<std::size_t>(y) *
            static_cast<std::size_t>(pyramidWidth_) +
        static_cast<std::size_t>(x)
    ];
}

std::size_t QuickSelectTool::pixelCount() const noexcept
{
    return static_cast<std::size_t>(width_) *
           static_cast<std::size_t>(height_);
}

void QuickSelectTool::clearMasks()
{
    std::fill(selectionMask_.begin(), selectionMask_.end(), 0);
    std::fill(foregroundConstraints_.begin(), foregroundConstraints_.end(), 0);
    std::fill(backgroundConstraints_.begin(), backgroundConstraints_.end(), 0);

    if (pyramidResizeRequired_) {
        std::fill(
            pyramidForegroundConstraints_.begin(),
            pyramidForegroundConstraints_.end(),
            0
        );

        std::fill(
            pyramidBackgroundConstraints_.begin(),
            pyramidBackgroundConstraints_.end(),
            0
        );

        std::fill(
            pyramidSelectionMask_.begin(),
            pyramidSelectionMask_.end(),
            0
        );
    }
}

void QuickSelectTool::setCurrentMask(
    const Byte* mask,
    std::size_t size
)
{
    if (!mask || size != pixelCount()) {
        throw std::invalid_argument(
            "QuickSelectTool::setCurrentMask: invalid mask"
        );
    }

    // Adobe QuickSelectTool::qsSetCurrMask(unsigned char*)
    //
    // Recovered configuration:
    //   +0xEC = 1
    //   ClearMasks()
    //   ReBuildNodesFromSegmentation()
    //   SetLocalEnergies(...)
    //
    // QuickSelectTool constructor fixes +0xD8 to 1.  This becomes
    // SetLocalEnergies param_7 == 1, which skips the param_18
    // current-mask processing branches.  The supplied mask pointer
    // therefore does not contribute the previously assumed graded
    // soft terminal prior.
    //
    // Keep the recovered reset semantics here.  The mask argument is
    // validated for API compatibility but is intentionally not copied
    // into the segmentation or converted into terminal constraints.
    (void)mask;

    foregroundMode_ = true;

    clearMasks();

    // qsSetCurrMask rebuilds Adobe's ImageGraph state immediately:
    // ReBuildNodesFromSegmentation(), SetLocalEnergies(), then
    // SetNeighborEnergiesRGBType() all complete before it returns.
    graphCut_.reset();
    graphInitialized_ = false;
    ensureGraphInitialized();
}

static void stampCircle(
    std::vector<QuickSelectTool::Byte>& mask,
    int width,
    int height,
    int cx,
    int cy,
    int radius,
    QuickSelectTool::Byte value
)
{
    if (radius < 0) {
        return;
    }

    const int minX = std::max(0, cx - radius);
    const int maxX = std::min(width - 1, cx + radius);
    const int minY = std::max(0, cy - radius);
    const int maxY = std::min(height - 1, cy + radius);

    const long long r2 =
        static_cast<long long>(radius) *
        static_cast<long long>(radius);

    for (int y = minY; y <= maxY; ++y) {
        for (int x = minX; x <= maxX; ++x) {
            const long long dx = x - cx;
            const long long dy = y - cy;

            if (dx * dx + dy * dy <= r2) {
                mask[
                    static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(width) +
                    static_cast<std::size_t>(x)
                ] = value;
            }
        }
    }
}

static void resizeMaskAdobe(
    const std::vector<QuickSelectTool::Byte>& source,
    int sourceWidth,
    int sourceHeight,
    std::vector<QuickSelectTool::Byte>& destination,
    int destinationWidth,
    int destinationHeight
)
{
    if (
        sourceWidth <= 0 ||
        sourceHeight <= 0 ||
        destinationWidth <= 0 ||
        destinationHeight <= 0
    ) {
        return;
    }

    const double scaleX =
        destinationWidth > 1
            ? static_cast<double>(sourceWidth - 1) /
                static_cast<double>(destinationWidth - 1)
            : 0.0;

    const double scaleY =
        destinationHeight > 1
            ? static_cast<double>(sourceHeight - 1) /
                static_cast<double>(destinationHeight - 1)
            : 0.0;

    for (int y = 0; y < destinationHeight; ++y) {
        const int sourceY =
            static_cast<int>(
                scaleY * static_cast<double>(y) + 0.5
            );

        for (int x = 0; x < destinationWidth; ++x) {
            const int sourceX =
                static_cast<int>(
                    scaleX * static_cast<double>(x) + 0.5
                );

            destination[
                static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(destinationWidth) +
                static_cast<std::size_t>(x)
            ] =
                source[
                    static_cast<std::size_t>(sourceY) *
                        static_cast<std::size_t>(sourceWidth) +
                    static_cast<std::size_t>(sourceX)
                ];
        }
    }
}

void QuickSelectTool::ensureGraphInitialized()
{
    if (graphInitialized_) {
        return;
    }

    std::vector<Byte>& workingForegroundConstraints =
        pyramidResizeRequired_
            ? pyramidForegroundConstraints_
            : foregroundConstraints_;

    std::vector<Byte>& workingBackgroundConstraints =
        pyramidResizeRequired_
            ? pyramidBackgroundConstraints_
            : backgroundConstraints_;

    graphCut_.initialize(
        pyramidImageRGB_.data(),
        pyramidWidth_,
        pyramidHeight_,
        workingForegroundConstraints.data(),
        workingBackgroundConstraints.data()
    );

    graphInitialized_ = true;
}


void QuickSelectTool::refineEdgesSegmentation()
{
    const int workingWidth = pyramidWidth_;
    const int workingHeight = pyramidHeight_;

    const std::size_t pixels =
        static_cast<std::size_t>(workingWidth) *
        static_cast<std::size_t>(workingHeight);

    std::vector<Byte>& workingSelectionMask =
        pyramidResizeRequired_
            ? pyramidSelectionMask_
            : selectionMask_;

    std::vector<Byte>& workingForegroundConstraints =
        pyramidResizeRequired_
            ? pyramidForegroundConstraints_
            : foregroundConstraints_;

    std::vector<Byte>& workingBackgroundConstraints =
        pyramidResizeRequired_
            ? pyramidBackgroundConstraints_
            : backgroundConstraints_;

    refinementChangedPixelCount_ = -1;
    refinementActiveTileCount_ = 0;
    refinementBuiltNodeCount_ = 0;
    refinementForcedForegroundNodeCount_ = 0;
    refinementForcedBackgroundNodeCount_ = 0;
    refinementNeighborLinkCount_ = 0;

    if (pixels == 0 ||
        pyramidImageRGB_.size() < pixels * 3 ||
        workingSelectionMask.size() < pixels ||
        workingForegroundConstraints.size() < pixels ||
        workingBackgroundConstraints.size() < pixels) {
        return;
    }

    detail::TiledRefinement refinement(workingWidth, workingHeight);

    const std::vector<detail::TiledRefinement::Point> boundary =
        refinement.getBoundaryPoints(workingSelectionMask.data());

    if (boundary.empty()) {
        return;
    }

    const std::vector<Byte> borderRegionMask =
        refinement.renderBorderRegionMask(boundary);

    const std::vector<detail::TiledRefinement::Point> foregroundBoundary =
        refinement.getBoundaryPointsInSelection(
            workingSelectionMask.data(),
            borderRegionMask.data(),
            false
        );

    const std::vector<detail::TiledRefinement::Point> backgroundBoundary =
        refinement.getBoundaryPointsInSelection(
            workingSelectionMask.data(),
            borderRegionMask.data(),
            true
        );

    refinement.createTilesAlong(boundary);
    refinement.createTilesAlong(foregroundBoundary);
    refinement.createTilesAlong(backgroundBoundary);

    refinement.buildNodesInMask(
        borderRegionMask.data(),
        workingForegroundConstraints.data(),
        workingBackgroundConstraints.data(),
        foregroundMode_
    );

    refinement.freezeForegroundAlong(foregroundBoundary);
    refinement.freezeBackgroundAlong(backgroundBoundary);

    const Byte* red = pyramidImageRGB_.data();
    const Byte* green = red + pixels;
    const Byte* blue = green + pixels;

    refinement.buildNeighborLinksRGB(red, green, blue);

    refinementActiveTileCount_ =
        refinement.activeTileCount();

    refinementBuiltNodeCount_ =
        refinement.builtNodeCount();

    refinementForcedForegroundNodeCount_ =
        refinement.forcedForegroundNodeCount();

    refinementForcedBackgroundNodeCount_ =
        refinement.forcedBackgroundNodeCount();

    refinementFrozenForegroundNodeCount_ =
        refinement.frozenForegroundNodeCount();

    refinementFrozenBackgroundNodeCount_ =
        refinement.frozenBackgroundNodeCount();

    refinementUniqueFrozenNodeCount_ =
        refinement.uniqueFrozenNodeCount();

    refinementOverlappingFrozenNodeCount_ =
        refinement.overlappingFrozenNodeCount();

    refinementNeighborLinkCount_ =
        refinement.neighborLinkCount();

    refinement.calculateMinimumCut();

    const std::vector<Byte> firstStageMask = workingSelectionMask;

    refinement.readSegmentationMask(
        workingSelectionMask.data(),
        workingSelectionMask.size()
    );

    refinementPreReadbackMismatchCount_ =
        refinement.preReadbackMismatchCount();

    int changed = 0;

    for (std::size_t i = 0; i < pixels; ++i) {
        if (workingSelectionMask[i] != firstStageMask[i]) {
            ++changed;
        }
    }

    refinementChangedPixelCount_ = changed;
}

void QuickSelectTool::refineEdges(int borderRadius)
{
    const std::size_t pixels = pixelCount();

    if (pixels == 0 ||
        imageRGB_.size() < pixels * 3 ||
        selectionMask_.size() < pixels ||
        foregroundConstraints_.size() < pixels ||
        backgroundConstraints_.size() < pixels) {
        return;
    }

    const double megapixelScale =
        std::sqrt(
            static_cast<double>(width_) *
            static_cast<double>(height_) /
            1000000.0
        );

    const int imageDerivedTileSize =
        static_cast<int>(std::ceil(megapixelScale));

    const int tileSize =
        std::max(borderRadius, imageDerivedTileSize);

    detail::TiledRefinement refinement(
        width_,
        height_,
        tileSize,
        8
    );

    const std::vector<detail::TiledRefinement::Point> boundary =
        refinement.getBoundaryPoints(selectionMask_.data());

    if (boundary.empty()) {
        return;
    }

    const std::vector<Byte> borderRegionMask =
        refinement.renderBorderMask(
            boundary,
            borderRadius
        );

    const std::vector<detail::TiledRefinement::Point> foregroundBoundary =
        refinement.getBoundaryPointsInSelection(
            selectionMask_.data(),
            borderRegionMask.data(),
            false
        );

    const std::vector<detail::TiledRefinement::Point> backgroundBoundary =
        refinement.getBoundaryPointsInSelection(
            selectionMask_.data(),
            borderRegionMask.data(),
            true
        );

    refinement.createTilesAlong(boundary);
    refinement.createTilesAlong(foregroundBoundary);
    refinement.createTilesAlong(backgroundBoundary);

    refinement.buildNodesInMask(
        borderRegionMask.data(),
        foregroundConstraints_.data(),
        backgroundConstraints_.data(),
        foregroundMode_
    );

    refinement.freezeForegroundAlong(foregroundBoundary);
    refinement.freezeBackgroundAlong(backgroundBoundary);

    const Byte* red = imageRGB_.data();
    const Byte* green = red + pixels;
    const Byte* blue = green + pixels;

    refinement.buildNeighborLinksRGB(red, green, blue);

    mouseUpActiveTileCount_ =
        refinement.activeTileCount();

    mouseUpBuiltNodeCount_ =
        refinement.builtNodeCount();

    mouseUpNeighborLinkCount_ =
        refinement.neighborLinkCount();

    refinement.calculateMinimumCut();

    refinement.readSegmentationMask(
        selectionMask_.data(),
        selectionMask_.size()
    );
}

QuickSelectTool::BrushRegion QuickSelectTool::stampBrushHit(
    int x,
    int y,
    int radius,
    int mode
)
{
    ensureGraphInitialized();

    const bool foreground =
        isForegroundMode(mode);

    std::vector<Byte>& workingForegroundConstraints =
        pyramidResizeRequired_
            ? pyramidForegroundConstraints_
            : foregroundConstraints_;

    std::vector<Byte>& workingBackgroundConstraints =
        pyramidResizeRequired_
            ? pyramidBackgroundConstraints_
            : backgroundConstraints_;

    const int workingWidth = pyramidWidth_;
    const int workingHeight = pyramidHeight_;

    // Adobe scales BrushHit coordinates by 1 / (1 << level),
    // rounds with +0.5, and clamps the working radius to >= 1.
    const double inverseScale =
        1.0 / static_cast<double>(1 << pyramidLevel_);

    const int workingX =
        static_cast<int>(
            static_cast<double>(x) * inverseScale + 0.5
        );

    const int workingY =
        static_cast<int>(
            static_cast<double>(y) * inverseScale + 0.5
        );

    int workingRadius =
        static_cast<int>(
            static_cast<double>(radius) * inverseScale + 0.5
        );

    if (workingRadius < 1) {
        workingRadius = 1;
    }

    stampCircle(
        workingForegroundConstraints,
        workingWidth,
        workingHeight,
        workingX,
        workingY,
        workingRadius,
        foreground ? 0xFF : 0x00
    );

    stampCircle(
        workingBackgroundConstraints,
        workingWidth,
        workingHeight,
        workingX,
        workingY,
        workingRadius,
        foreground ? 0x00 : 0xFF
    );

    // When a pyramid exists Adobe stamps the original-resolution
    // masks independently using the unscaled brush.
    if (pyramidResizeRequired_) {
        stampCircle(
            foregroundConstraints_,
            width_,
            height_,
            x,
            y,
            radius,
            foreground ? 0xFF : 0x00
        );

        stampCircle(
            backgroundConstraints_,
            width_,
            height_,
            x,
            y,
            radius,
            foreground ? 0x00 : 0xFF
        );
    }

    const int minX =
        std::max(0, workingX - workingRadius);

    const int maxX =
        std::min(
            workingWidth - 1,
            workingX + workingRadius
        );

    const int minY =
        std::max(0, workingY - workingRadius);

    const int maxY =
        std::min(
            workingHeight - 1,
            workingY + workingRadius
        );

    if (minX > maxX || minY > maxY) {
        return {};
    }

    BrushRegion region {
        minX,
        minY,
        maxX - minX + 1,
        maxY - minY + 1
    };

    // Adobe offsets the WORKING constraint-mask pointers to the
    // top-left of the affected working-resolution rectangle.
    const std::size_t regionOffset =
        static_cast<std::size_t>(region.y) *
            static_cast<std::size_t>(workingWidth) +
        static_cast<std::size_t>(region.x);

    graphCut_.updateFrozenEnergies(
        workingForegroundConstraints.data() + regionOffset,
        workingBackgroundConstraints.data() + regionOffset,
        region.x,
        region.y,
        region.width,
        region.height
    );

    return region;
}

void QuickSelectTool::brushHit(
    int x,
    int y,
    int radius,
    int mode
)
{
    stampBrushHit(
        x,
        y,
        radius,
        mode
    );

    // Adobe single BrushHit performs one minimum cut after
    // SetFrozenEnergies for the affected brush region.
    std::vector<Byte>& workingSelectionMask =
        pyramidResizeRequired_
            ? pyramidSelectionMask_
            : selectionMask_;

    graphCut_.calculateMinimumCut(
        workingSelectionMask
    );

    refineEdgesSegmentation();

    if (pyramidResizeRequired_) {
        resizeMaskAdobe(
            pyramidSelectionMask_,
            pyramidWidth_,
            pyramidHeight_,
            selectionMask_,
            width_,
            height_
        );
    }
}

void QuickSelectTool::brushHits(
    const int* x,
    const int* y,
    const int* radius,
    const int* mode,
    int count
)
{
    if (!x || !y || !radius || !mode || count <= 0) {
        return;
    }

    for (int i = 0; i < count; ++i) {
        stampBrushHit(
            x[i],
            y[i],
            radius[i],
            mode[i]
        );
    }

    // Adobe multi-hit BrushHit calls SetFrozenEnergies after each
    // individual stamp, then CalculateMinimumCut exactly once.
    std::vector<Byte>& workingSelectionMask =
        pyramidResizeRequired_
            ? pyramidSelectionMask_
            : selectionMask_;

    graphCut_.calculateMinimumCut(
        workingSelectionMask
    );

    refineEdgesSegmentation();

    if (pyramidResizeRequired_) {
        resizeMaskAdobe(
            pyramidSelectionMask_,
            pyramidWidth_,
            pyramidHeight_,
            selectionMask_,
            width_,
            height_
        );
    }
}

void QuickSelectTool::qsBrushHits(
    const int* x,
    const int* y,
    const int* radius,
    int mode,
    int count
)
{
    foregroundMode_ =
        isForegroundMode(mode);

    if (!x || !y || !radius || count <= 0) {
        return;
    }

    for (int i = 0; i < count; ++i) {
        stampBrushHit(
            x[i],
            y[i],
            radius[i],
            mode
        );
    }

    // Active Photoshop Touch QS path:
    // SetFrozenEnergies per sample, one CalculateMinimumCut per batch.
    std::vector<Byte>& workingSelectionMask =
        pyramidResizeRequired_
            ? pyramidSelectionMask_
            : selectionMask_;

    graphCut_.calculateMinimumCut(
        workingSelectionMask
    );

    refineEdgesSegmentation();

    if (pyramidResizeRequired_) {
        resizeMaskAdobe(
            pyramidSelectionMask_,
            pyramidWidth_,
            pyramidHeight_,
            selectionMask_,
            width_,
            height_
        );
    }
}

void QuickSelectTool::switchMode(int mode)
{

    std::vector<Byte>& workingForegroundConstraints =
        pyramidResizeRequired_
            ? pyramidForegroundConstraints_
            : foregroundConstraints_;

    std::vector<Byte>& workingBackgroundConstraints =
        pyramidResizeRequired_
            ? pyramidBackgroundConstraints_
            : backgroundConstraints_;

    foregroundMode_ = isForegroundMode(mode);

    if (graphInitialized_) {
        graphCut_.switchMode(
            workingForegroundConstraints.data(),
            workingBackgroundConstraints.data(),
            foregroundMode_
        );
    }
}

void QuickSelectTool::reset(int mode)
{
    foregroundMode_ = isForegroundMode(mode);
    clearMasks();

    // Adobe QuickSelectionReset immediately performs:
    //
    //   ClearMasks
    //   ReBuildNodesFromSegmentation
    //   SetLocalEnergies
    //   SetNeighborEnergiesRGBType
    //
    // Recreate that lifecycle here rather than deferring graph
    // reconstruction until the next brush operation.
    graphCut_.reset();
    graphInitialized_ = false;
    ensureGraphInitialized();
}

const QuickSelectTool::Byte*
QuickSelectTool::getMask() const noexcept
{
    return selectionMask_.data();
}

int QuickSelectTool::freshCutMismatchCount() const noexcept
{
    return graphCut_.freshCutMismatchCount();
}

int QuickSelectTool::refinementChangedPixelCount() const noexcept
{
    return refinementChangedPixelCount_;
}

int QuickSelectTool::refinementPreReadbackMismatchCount() const noexcept
{
    return refinementPreReadbackMismatchCount_;
}

int QuickSelectTool::refinementActiveTileCount() const noexcept
{
    return refinementActiveTileCount_;
}

int QuickSelectTool::refinementBuiltNodeCount() const noexcept
{
    return refinementBuiltNodeCount_;
}

int QuickSelectTool::refinementForcedForegroundNodeCount() const noexcept
{
    return refinementForcedForegroundNodeCount_;
}

int QuickSelectTool::refinementForcedBackgroundNodeCount() const noexcept
{
    return refinementForcedBackgroundNodeCount_;
}

int QuickSelectTool::refinementFrozenForegroundNodeCount() const noexcept
{
    return refinementFrozenForegroundNodeCount_;
}

int QuickSelectTool::refinementFrozenBackgroundNodeCount() const noexcept
{
    return refinementFrozenBackgroundNodeCount_;
}

int QuickSelectTool::refinementUniqueFrozenNodeCount() const noexcept
{
    return refinementUniqueFrozenNodeCount_;
}

int QuickSelectTool::refinementOverlappingFrozenNodeCount() const noexcept
{
    return refinementOverlappingFrozenNodeCount_;
}

int QuickSelectTool::refinementNeighborLinkCount() const noexcept
{
    return refinementNeighborLinkCount_;
}

int QuickSelectTool::mouseUpActiveTileCount() const noexcept
{
    return mouseUpActiveTileCount_;
}

int QuickSelectTool::mouseUpBuiltNodeCount() const noexcept
{
    return mouseUpBuiltNodeCount_;
}

int QuickSelectTool::mouseUpNeighborLinkCount() const noexcept
{
    return mouseUpNeighborLinkCount_;
}

const std::vector<float>&
QuickSelectTool::diagnosticSourceCapacities() const noexcept
{
    return graphCut_.diagnosticSourceCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticSinkCapacities() const noexcept
{
    return graphCut_.diagnosticSinkCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticRequestedSourceCapacities() const noexcept
{
    return graphCut_.diagnosticRequestedSourceCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticRequestedSinkCapacities() const noexcept
{
    return graphCut_.diagnosticRequestedSinkCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticBeforeUpdateSourceCapacities() const noexcept
{
    return graphCut_.diagnosticBeforeUpdateSourceCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticBeforeUpdateSinkCapacities() const noexcept
{
    return graphCut_.diagnosticBeforeUpdateSinkCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticAfterUpdateSourceCapacities() const noexcept
{
    return graphCut_.diagnosticAfterUpdateSourceCapacities();
}

const std::vector<float>&
QuickSelectTool::diagnosticAfterUpdateSinkCapacities() const noexcept
{
    return graphCut_.diagnosticAfterUpdateSinkCapacities();
}


const std::vector<float>&
QuickSelectTool::diagnosticBeforeUpdateSourceResiduals() const noexcept
{
    return graphCut_.diagnosticBeforeUpdateSourceResiduals();
}

const std::vector<float>&
QuickSelectTool::diagnosticBeforeUpdateSinkResiduals() const noexcept
{
    return graphCut_.diagnosticBeforeUpdateSinkResiduals();
}

const std::vector<float>&
QuickSelectTool::diagnosticAfterUpdateSourceResiduals() const noexcept
{
    return graphCut_.diagnosticAfterUpdateSourceResiduals();
}

const std::vector<float>&
QuickSelectTool::diagnosticAfterUpdateSinkResiduals() const noexcept
{
    return graphCut_.diagnosticAfterUpdateSinkResiduals();
}

const std::vector<float>&
QuickSelectTool::diagnosticHorizontalCapacities() const noexcept
{
    return graphCut_.diagnosticHorizontalCapacities();
}

const QuickSelectTool::Byte*
QuickSelectTool::mouseUp()
{
    if (pyramidResizeRequired_) {
        const int borderRadius = 1 << pyramidLevel_;
        refineEdges(borderRadius);
    }

    return selectionMask_.data();
}

} // namespace pstouch::quickselection
