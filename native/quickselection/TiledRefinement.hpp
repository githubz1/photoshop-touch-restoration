#pragma once

#include <cstdint>
#include <memory>
#include <vector>

#include "SparseGraph.hpp"
#include "GraphCut.hpp"

namespace pstouch::quickselection::detail {

class TiledRefinement {
public:
    using Byte = std::uint8_t;

    struct Point {
        int x = 0;
        int y = 0;
    };

    struct Tile {
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        bool active = false;
        std::vector<int> nodeMap;
    };

    static constexpr int kDefaultTileSize = 11;
    static constexpr int kDefaultTopology = 4;

    int tileSize_ = kDefaultTileSize;
    int topology_ = kDefaultTopology;

    TiledRefinement(int width, int height);

    TiledRefinement(
        int width,
        int height,
        int tileSize,
        int topology
    );

    std::vector<Point> getBoundaryPoints(
        const Byte* mask
    ) const;

    std::vector<Byte> renderBorderRegionMask(
        const std::vector<Point>& boundaryPoints
    ) const;

    std::vector<Byte> renderBorderMask(
        const std::vector<Point>& boundaryPoints,
        int radius
    ) const;

    std::vector<Point> getBoundaryPointsInSelection(
        const Byte* selectionMask,
        const Byte* boundaryMask,
        bool nonZeroSelection
    ) const;

    int tileColumns() const noexcept;
    int tileRows() const noexcept;
    int tileCount() const noexcept;

    int tileIndexForPoint(int x, int y) const noexcept;

    void createTilesAlong(const std::vector<Point>& points);

    int buildNodesInMask(
        const Byte* qualificationMask,
        const Byte* foregroundConstraints,
        const Byte* backgroundConstraints,
        bool foregroundMode,
        Byte qualificationThreshold = 0x7f,
        Byte constraintThreshold = 0x7f
    );

    void freezeForegroundAlong(
        const std::vector<Point>& points
    );

    void freezeBackgroundAlong(
        const std::vector<Point>& points
    );

    int buildNeighborLinksRGB(
        const Byte* red,
        const Byte* green,
        const Byte* blue
    );

    float calculateMinimumCut();

    void readSegmentationMask(
        Byte* segmentationMask,
        std::size_t size
    ) const;

    const std::vector<Tile>& tiles() const noexcept { return tiles_; }
    int activeTileCount() const noexcept;

    int builtNodeCount() const noexcept {
        return builtNodeCount_;
    }

    int forcedForegroundNodeCount() const noexcept {
        return forcedForegroundNodeCount_;
    }

    int forcedBackgroundNodeCount() const noexcept {
        return forcedBackgroundNodeCount_;
    }

    int frozenForegroundNodeCount() const noexcept {
        return static_cast<int>(frozenForegroundNodes_.size());
    }

    int frozenBackgroundNodeCount() const noexcept {
        return static_cast<int>(frozenBackgroundNodes_.size());
    }

    int uniqueFrozenNodeCount() const noexcept;
    int overlappingFrozenNodeCount() const noexcept;

    int neighborLinkCount() const noexcept {
        return neighborLinkCount_;
    }

    int preReadbackMismatchCount() const noexcept {
        return preReadbackMismatchCount_;
    }

private:
    bool inBounds(int x, int y) const noexcept;
    Byte pixel(const Byte* mask, int x, int y) const noexcept;

    static constexpr int kNoNode = -1;

    void activateTile(int index);
    int nodeAt(int x, int y) const noexcept;
    int forceNodeAt(int x, int y);

    int width_ = 0;
    int height_ = 0;

    std::unique_ptr<SparseGraph> graph_;
    int sourceNode_ = -1;
    int sinkNode_ = -1;

    std::vector<Tile> tiles_;

    int builtNodeCount_ = 0;
    int forcedForegroundNodeCount_ = 0;
    int forcedBackgroundNodeCount_ = 0;
    std::vector<int> frozenForegroundNodes_;
    std::vector<int> frozenBackgroundNodes_;
    int neighborLinkCount_ = 0;
    mutable int preReadbackMismatchCount_ = -1;
};

} // namespace pstouch::quickselection::detail
