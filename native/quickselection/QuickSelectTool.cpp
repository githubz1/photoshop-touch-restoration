#include "QuickSelectTool.hpp"
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

    // Recovered Adobe qsSetCurrMask() behavior:
    //
    //   +0xEC = 1
    //   ClearMasks()
    //   ReBuildNodesFromSegmentation()
    //   SetLocalEnergies(..., currentMask, ...)
    //
    // The supplied mask is a SOFT graph prior, not a hard scribble
    // constraint and not simply the final output mask.
    foregroundMode_ = true;

    std::fill(
        foregroundConstraints_.begin(),
        foregroundConstraints_.end(),
        0
    );

    std::fill(
        backgroundConstraints_.begin(),
        backgroundConstraints_.end(),
        0
    );

    std::fill(
        selectionMask_.begin(),
        selectionMask_.end(),
        0
    );

    initialMask_.assign(mask, mask + size);
    hasInitialMask_ = true;
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

void QuickSelectTool::brushHit(
    int x,
    int y,
    int radius,
    int mode
)
{
    const bool foreground = isForegroundMode(mode);

    // Proven ARMv7 behavior:
    //
    // mode 0:
    //   foreground constraint = FF
    //   background constraint = 00
    //
    // nonzero:
    //   foreground constraint = 00
    //   background constraint = FF

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
        brushHit(x[i], y[i], radius[i], mode[i]);
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
    foregroundMode_ = isForegroundMode(mode);

    if (!x || !y || !radius || count <= 0) {
        return;
    }

    for (int i = 0; i < count; ++i) {
        brushHit(x[i], y[i], radius[i], mode);
    }
}

void QuickSelectTool::switchMode(int mode)
{
    foregroundMode_ = isForegroundMode(mode);
}

void QuickSelectTool::reset(int mode)
{
    foregroundMode_ = isForegroundMode(mode);
    clearMasks();
}

const QuickSelectTool::Byte*
QuickSelectTool::getMask() const noexcept
{
    return selectionMask_.data();
}

const QuickSelectTool::Byte*
QuickSelectTool::mouseUp()
{
    GraphCut::segment(
        imageRGB_.data(),
        width_,
        height_,
        foregroundConstraints_.data(),
        backgroundConstraints_.data(),
        hasInitialMask_ ? initialMask_.data() : nullptr,
        selectionMask_
    );

    return selectionMask_.data();
}

} // namespace pstouch::quickselection
