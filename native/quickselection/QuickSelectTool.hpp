#pragma once

#include <cstddef>
#include <cstdint>
#include <vector>

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
    const Byte* mouseUp();

    int width() const noexcept { return width_; }
    int height() const noexcept { return height_; }

private:
    static bool isForegroundMode(int mode) noexcept
    {
        // Proven from the ARMv7 implementation:
        //
        // mode == 0  -> foreground/add
        // mode != 0  -> background/remove
        return mode == 0;
    }

    std::size_t pixelCount() const noexcept;

    int width_ = 0;
    int height_ = 0;

    // Original QuickSelect input is a three-channel image.
    std::vector<Byte> imageRGB_;

    // Recovered semantic equivalents of the important native buffers.
    //
    // ARMv7 +0x68 -> final selection mask
    // ARMv7 +0x74 -> foreground constraint/scribble mask
    // ARMv7 +0x78 -> background constraint/scribble mask
    std::vector<Byte> selectionMask_;

    std::vector<Byte> foregroundConstraints_;
    std::vector<Byte> backgroundConstraints_;

    // Semantic equivalent of ARMv7 QuickSelectTool +0xEC.
    bool foregroundMode_ = true;
};

} // namespace pstouch::quickselection
