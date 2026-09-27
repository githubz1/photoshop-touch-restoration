#include "TiledRefinement.hpp"

#include <limits>
#include <cmath>

#include <algorithm>

namespace pstouch::quickselection::detail {

TiledRefinement::TiledRefinement(int width, int height)
    : TiledRefinement(
          width,
          height,
          kDefaultTileSize,
          kDefaultTopology
      )
{
}

TiledRefinement::TiledRefinement(
    int width,
    int height,
    int tileSize,
    int topology
)
    : tileSize_(std::max(tileSize, 1)),
      topology_(topology),
      width_(std::max(width, 0)),
      height_(std::max(height, 0)),
      graph_(makeSparseGraph(0))
{
    sourceNode_ = graph_->addNode();
    sinkNode_ = graph_->addNode();
    graph_->setTerminals(sourceNode_, sinkNode_);

    tiles_.reserve(
        static_cast<std::size_t>(tileCount())
    );

    for (int row = 0; row < tileRows(); ++row) {
        for (int column = 0; column < tileColumns(); ++column) {
            const int left = column * tileSize_;
            const int top = row * tileSize_;

            tiles_.push_back({
                left,
                top,
                std::min(left + tileSize_, width_),
                std::min(top + tileSize_, height_),
                false,
                {}
            });
        }
    }
}

bool TiledRefinement::inBounds(int x, int y) const noexcept
{
    return x >= 0 &&
           y >= 0 &&
           x < width_ &&
           y < height_;
}

TiledRefinement::Byte
TiledRefinement::pixel(
    const Byte* mask,
    int x,
    int y
) const noexcept
{
    if (!mask || !inBounds(x, y)) {
        return 0;
    }

    return mask[
        static_cast<std::size_t>(y) *
            static_cast<std::size_t>(width_) +
        static_cast<std::size_t>(x)
    ];
}

std::vector<TiledRefinement::Point>
TiledRefinement::getBoundaryPoints(
    const Byte* mask
) const
{
    std::vector<Point> points;

    if (!mask || width_ <= 0 || height_ <= 0) {
        return points;
    }

    for (int y = 0; y < height_; ++y) {
        for (int x = 0; x < width_; ++x) {
            const bool center =
                pixel(mask, x, y) != 0;

            bool boundary = false;

            // Recovered GetBoundaryPoints uses the complete
            // 8-neighbourhood. At an image edge, the unavailable
            // neighbour is represented by the current pixel.
            for (int dy = -1; dy <= 1 && !boundary; ++dy) {
                for (int dx = -1; dx <= 1; ++dx) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    const int nx = x + dx;
                    const int ny = y + dy;

                    const bool neighbour =
                        inBounds(nx, ny)
                            ? pixel(mask, nx, ny) != 0
                            : center;

                    if (neighbour != center) {
                        boundary = true;
                        break;
                    }
                }
            }

            if (boundary) {
                points.push_back({x, y});
            }
        }
    }

    return points;
}

std::vector<TiledRefinement::Byte>
TiledRefinement::renderBorderRegionMask(
    const std::vector<Point>& boundaryPoints
) const
{
    std::vector<Byte> mask(
        static_cast<std::size_t>(width_) *
            static_cast<std::size_t>(height_),
        0
    );

    for (const Point& point : boundaryPoints) {
        if (!inBounds(point.x, point.y)) {
            continue;
        }

        const std::size_t index =
            static_cast<std::size_t>(point.y) *
                static_cast<std::size_t>(width_) +
            static_cast<std::size_t>(point.x);

        mask[index] = 0xff;
    }

    return mask;
}

std::vector<TiledRefinement::Byte>
TiledRefinement::renderBorderMask(
    const std::vector<Point>& boundaryPoints,
    int radius
) const
{
    std::vector<Byte> mask(
        static_cast<std::size_t>(width_) *
            static_cast<std::size_t>(height_),
        0
    );

    if (width_ <= 0 || height_ <= 0 || radius < 0) {
        return mask;
    }

    const std::int64_t radiusSquared =
        static_cast<std::int64_t>(radius) *
        static_cast<std::int64_t>(radius);

    for (const Point& point : boundaryPoints) {
        const int minX = std::max(0, point.x - radius);
        const int maxX = std::min(width_ - 1, point.x + radius);
        const int minY = std::max(0, point.y - radius);
        const int maxY = std::min(height_ - 1, point.y + radius);

        if (minX > maxX || minY > maxY) {
            continue;
        }

        for (int y = minY; y <= maxY; ++y) {
            const std::int64_t dy =
                static_cast<std::int64_t>(y) -
                static_cast<std::int64_t>(point.y);

            for (int x = minX; x <= maxX; ++x) {
                const std::int64_t dx =
                    static_cast<std::int64_t>(x) -
                    static_cast<std::int64_t>(point.x);

                if (dx * dx + dy * dy <= radiusSquared) {
                    const std::size_t index =
                        static_cast<std::size_t>(y) *
                            static_cast<std::size_t>(width_) +
                        static_cast<std::size_t>(x);

                    mask[index] = 0xff;
                }
            }
        }
    }

    return mask;
}

std::vector<TiledRefinement::Point>
TiledRefinement::getBoundaryPointsInSelection(
    const Byte* selectionMask,
    const Byte* boundaryMask,
    bool nonZeroSelection
) const
{
    std::vector<Point> points;

    if (!selectionMask ||
        !boundaryMask ||
        width_ <= 0 ||
        height_ <= 0) {
        return points;
    }

    for (int y = 0; y < height_; ++y) {
        for (int x = 0; x < width_; ++x) {
            const std::size_t index =
                static_cast<std::size_t>(y) *
                    static_cast<std::size_t>(width_) +
                static_cast<std::size_t>(x);

            const bool selected =
                selectionMask[index] != 0;

            if (selected != nonZeroSelection) {
                continue;
            }

            const bool center =
                boundaryMask[index] != 0;

            bool boundary = false;

            for (int dy = -1; dy <= 1 && !boundary; ++dy) {
                for (int dx = -1; dx <= 1; ++dx) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    const int nx = x + dx;
                    const int ny = y + dy;

                    const bool neighbour =
                        inBounds(nx, ny)
                            ? pixel(boundaryMask, nx, ny) != 0
                            : center;

                    if (neighbour != center) {
                        boundary = true;
                        break;
                    }
                }
            }

            if (boundary) {
                points.push_back({x, y});
            }
        }
    }

    return points;
}

int TiledRefinement::tileColumns() const noexcept
{
    if (width_ <= 0) {
        return 0;
    }

    return (width_ + tileSize_ - 1) / tileSize_;
}

int TiledRefinement::tileRows() const noexcept
{
    if (height_ <= 0) {
        return 0;
    }

    return (height_ + tileSize_ - 1) / tileSize_;
}

int TiledRefinement::tileCount() const noexcept
{
    return tileColumns() * tileRows();
}

int TiledRefinement::tileIndexForPoint(
    int x,
    int y
) const noexcept
{
    if (!inBounds(x, y)) {
        return -1;
    }

    const int column = x / tileSize_;
    const int row = y / tileSize_;

    return row * tileColumns() + column;
}

void TiledRefinement::createTilesAlong(
    const std::vector<Point>& points
)
{
    for (const Point& point : points) {
        const int index =
            tileIndexForPoint(point.x, point.y);

        if (index < 0) {
            continue;
        }

        activateTile(index);
    }
}

void TiledRefinement::activateTile(int index)
{
    Tile& tile = tiles_[static_cast<std::size_t>(index)];

    if (tile.active) {
        return;
    }

    tile.active = true;

    const int tileWidth = tile.right - tile.left;
    const int tileHeight = tile.bottom - tile.top;

    tile.nodeMap.assign(
        static_cast<std::size_t>(tileWidth) *
            static_cast<std::size_t>(tileHeight),
        kNoNode
    );
}

int TiledRefinement::buildNodesInMask(
    const Byte* qualificationMask,
    const Byte* foregroundConstraints,
    const Byte* backgroundConstraints,
    bool foregroundMode,
    Byte qualificationThreshold,
    Byte constraintThreshold
)
{
    if (!qualificationMask ||
        !foregroundConstraints ||
        !backgroundConstraints ||
        !graph_) {
        return 0;
    }

    int created = 0;

    for (Tile& tile : tiles_) {
        if (!tile.active) {
            continue;
        }

        const int tileWidth = tile.right - tile.left;
        const int tileHeight = tile.bottom - tile.top;

        if (tileWidth <= 0 || tileHeight <= 0) {
            continue;
        }

        const std::size_t required =
            static_cast<std::size_t>(tileWidth) *
            static_cast<std::size_t>(tileHeight);

        if (tile.nodeMap.size() != required) {
            tile.nodeMap.assign(required, kNoNode);
        }

        for (int localY = 0; localY < tileHeight; ++localY) {
            const int y = tile.top + localY;

            for (int localX = 0; localX < tileWidth; ++localX) {
                const int x = tile.left + localX;

                const std::size_t imageIndex =
                    static_cast<std::size_t>(y) *
                        static_cast<std::size_t>(width_) +
                    static_cast<std::size_t>(x);

                if (qualificationMask[imageIndex] <=
                    qualificationThreshold) {
                    continue;
                }

                const std::size_t tileIndex =
                    static_cast<std::size_t>(localY) *
                        static_cast<std::size_t>(tileWidth) +
                    static_cast<std::size_t>(localX);

                if (tile.nodeMap[tileIndex] != kNoNode) {
                    continue;
                }

                const int node = graph_->addNode();

                // Match ImageGraph::Create/BuildNodes:
                // source -> node and node -> sink begin at zero.
                graph_->addEdge(
                    sourceNode_,
                    node,
                    0.0f,
                    0.0f
                );

                graph_->addEdge(
                    node,
                    sinkNode_,
                    0.0f,
                    0.0f
                );

                tile.nodeMap[tileIndex] = node;

                const bool foregroundConstraint =
                    foregroundConstraints[imageIndex] >
                    constraintThreshold;

                const bool backgroundConstraint =
                    backgroundConstraints[imageIndex] >
                    constraintThreshold;

                const GraphCut::TerminalWeights terminal =
                    GraphCut::adobeRefinementTerminalWeights(
                        foregroundConstraint,
                        backgroundConstraint,
                        foregroundMode
                    );

                // BuildNodesAndSetLocalEnergiesInMask calls Adobe's
                // update_tweights(graph,node,fVar16,fVar15,...).
                // In our recovered representation terminal1 is source
                // and terminal0 is sink.
                graph_->updateTweights(
                    node,
                    static_cast<float>(terminal.terminal1),
                    static_cast<float>(terminal.terminal0)
                );

                ++created;
            }
        }
    }

    builtNodeCount_ = created;
    return created;
}

int TiledRefinement::nodeAt(int x, int y) const noexcept
{
    if (!inBounds(x, y)) {
        return kNoNode;
    }

    const int tileIndex = tileIndexForPoint(x, y);

    if (tileIndex < 0 ||
        tileIndex >= static_cast<int>(tiles_.size())) {
        return kNoNode;
    }

    const Tile& tile =
        tiles_[static_cast<std::size_t>(tileIndex)];

    if (!tile.active) {
        return kNoNode;
    }

    const int tileWidth = tile.right - tile.left;
    const int tileHeight = tile.bottom - tile.top;

    if (tileWidth <= 0 || tileHeight <= 0) {
        return kNoNode;
    }

    const int localX = x - tile.left;
    const int localY = y - tile.top;

    if (localX < 0 ||
        localY < 0 ||
        localX >= tileWidth ||
        localY >= tileHeight) {
        return kNoNode;
    }

    const std::size_t localIndex =
        static_cast<std::size_t>(localY) *
            static_cast<std::size_t>(tileWidth) +
        static_cast<std::size_t>(localX);

    if (localIndex >= tile.nodeMap.size()) {
        return kNoNode;
    }

    return tile.nodeMap[localIndex];
}

int TiledRefinement::forceNodeAt(int x, int y)
{
    if (!graph_ || !inBounds(x, y)) {
        return kNoNode;
    }

    const int tileIndex = tileIndexForPoint(x, y);

    if (tileIndex < 0 ||
        tileIndex >= static_cast<int>(tiles_.size())) {
        return kNoNode;
    }

    Tile& tile = tiles_[static_cast<std::size_t>(tileIndex)];

    // Adobe's BuildAndFreeze*NodeAt does not create the tile.
    // It only operates on an ImageGraph that was already created
    // by CreateImageGraphsAlong.
    if (!tile.active) {
        return kNoNode;
    }

    const int tileWidth = tile.right - tile.left;
    const int tileHeight = tile.bottom - tile.top;

    if (tileWidth <= 0 || tileHeight <= 0) {
        return kNoNode;
    }

    const int localX = x - tile.left;
    const int localY = y - tile.top;

    if (localX < 0 ||
        localY < 0 ||
        localX >= tileWidth ||
        localY >= tileHeight) {
        return kNoNode;
    }

    const std::size_t required =
        static_cast<std::size_t>(tileWidth) *
        static_cast<std::size_t>(tileHeight);

    if (tile.nodeMap.size() != required) {
        tile.nodeMap.assign(required, kNoNode);
    }

    const std::size_t localIndex =
        static_cast<std::size_t>(localY) *
            static_cast<std::size_t>(tileWidth) +
        static_cast<std::size_t>(localX);

    int& node = tile.nodeMap[localIndex];

    if (node != kNoNode) {
        return node;
    }

    node = graph_->addNode();

    // Exact sentinel-node creation performed by Adobe's
    // BuildAndFreezeForeground/BackgroundNodeAt:
    // create the shared graph node and attach zero-capacity
    // source/node and node/sink terminal edges.
    graph_->addEdge(
        sourceNode_,
        node,
        0.0f,
        0.0f
    );

    graph_->addEdge(
        node,
        sinkNode_,
        0.0f,
        0.0f
    );

    return node;
}

void TiledRefinement::freezeForegroundAlong(
    const std::vector<Point>& points
)
{
    if (!graph_) {
        return;
    }

    constexpr float hardConstraint =
        std::numeric_limits<float>::max() / 2.0f;

    for (const Point& point : points) {
        const int before = nodeAt(point.x, point.y);
        const int node = forceNodeAt(point.x, point.y);

        if (node == kNoNode) {
            continue;
        }

        if (before == kNoNode) {
            ++forcedForegroundNodeCount_;
        }

        if (std::find(
                frozenForegroundNodes_.begin(),
                frozenForegroundNodes_.end(),
                node
            ) == frozenForegroundNodes_.end()) {
            frozenForegroundNodes_.push_back(node);
        }

        // Adobe:
        // update_tweights(graph,node,0,FLT_MAX/2,0,1)
        graph_->updateTweights(
            node,
            0.0f,
            hardConstraint
        );
    }
}

void TiledRefinement::freezeBackgroundAlong(
    const std::vector<Point>& points
)
{
    if (!graph_) {
        return;
    }

    constexpr float hardConstraint =
        std::numeric_limits<float>::max() / 2.0f;

    for (const Point& point : points) {
        const int before = nodeAt(point.x, point.y);
        const int node = forceNodeAt(point.x, point.y);

        if (node == kNoNode) {
            continue;
        }

        if (before == kNoNode) {
            ++forcedBackgroundNodeCount_;
        }

        if (std::find(
                frozenBackgroundNodes_.begin(),
                frozenBackgroundNodes_.end(),
                node
            ) == frozenBackgroundNodes_.end()) {
            frozenBackgroundNodes_.push_back(node);
        }

        // Adobe:
        // update_tweights(graph,node,FLT_MAX/2,0,0,1)
        graph_->updateTweights(
            node,
            hardConstraint,
            0.0f
        );
    }
}

int TiledRefinement::uniqueFrozenNodeCount() const noexcept
{
    std::vector<int> nodes = frozenForegroundNodes_;

    for (const int node : frozenBackgroundNodes_) {
        if (std::find(nodes.begin(), nodes.end(), node) == nodes.end()) {
            nodes.push_back(node);
        }
    }

    return static_cast<int>(nodes.size());
}

int TiledRefinement::overlappingFrozenNodeCount() const noexcept
{
    int count = 0;

    for (const int node : frozenForegroundNodes_) {
        if (std::find(
                frozenBackgroundNodes_.begin(),
                frozenBackgroundNodes_.end(),
                node
            ) != frozenBackgroundNodes_.end()) {
            ++count;
        }
    }

    return count;
}

int TiledRefinement::buildNeighborLinksRGB(
    const Byte* red,
    const Byte* green,
    const Byte* blue
)
{
    if (!graph_ || !red || !green || !blue) {
        return 0;
    }

    constexpr double weightScale = 500.0;
    constexpr double beta =
        0.5 / (16.0 * 16.0);

    // Exact Adobe ImageGraph topology tables recovered from
    // SetNeighborOffsets / SetNeighborInverseDistances.
    //
    // topology 4:
    //   (-1, 0) * 1
    //   ( 0,-1) * 1
    //
    // topology 8:
    //   (-1, 0) * 1
    //   (-1,-1) * 1/sqrt(2)
    //   ( 0,-1) * 1
    //   (+1,-1) * 1/sqrt(2)
    //
    // Each undirected pair is visited exactly once.
    constexpr double diagonalInverseDistance =
        0.70710678118654752440;

    constexpr int topology4Dx[2] = {-1, 0};
    constexpr int topology4Dy[2] = {0, -1};
    constexpr double topology4InverseDistance[2] = {
        1.0,
        1.0
    };

    constexpr int topology8Dx[4] = {-1, -1, 0, 1};
    constexpr int topology8Dy[4] = {0, -1, -1, -1};
    constexpr double topology8InverseDistance[4] = {
        1.0,
        diagonalInverseDistance,
        1.0,
        diagonalInverseDistance
    };

    const int* dx = nullptr;
    const int* dy = nullptr;
    const double* inverseDistance = nullptr;
    int neighborCount = 0;

    if (topology_ == 4) {
        dx = topology4Dx;
        dy = topology4Dy;
        inverseDistance = topology4InverseDistance;
        neighborCount = 2;
    } else if (topology_ == 8) {
        dx = topology8Dx;
        dy = topology8Dy;
        inverseDistance = topology8InverseDistance;
        neighborCount = 4;
    } else {
        return 0;
    }

    int created = 0;

    for (const Tile& tile : tiles_) {
        if (!tile.active) {
            continue;
        }

        for (int y = tile.top; y < tile.bottom; ++y) {
            for (int x = tile.left; x < tile.right; ++x) {
                const int node = nodeAt(x, y);

                if (node == kNoNode) {
                    continue;
                }

                const std::size_t index =
                    static_cast<std::size_t>(y) *
                        static_cast<std::size_t>(width_) +
                    static_cast<std::size_t>(x);

                for (int n = 0; n < neighborCount; ++n) {
                    const int nx = x + dx[n];
                    const int ny = y + dy[n];

                    const int neighborNode =
                        nodeAt(nx, ny);

                    if (neighborNode == kNoNode) {
                        continue;
                    }

                    const std::size_t neighborIndex =
                        static_cast<std::size_t>(ny) *
                            static_cast<std::size_t>(width_) +
                        static_cast<std::size_t>(nx);

                    const double dr =
                        static_cast<double>(red[index]) -
                        static_cast<double>(red[neighborIndex]);

                    const double dg =
                        static_cast<double>(green[index]) -
                        static_cast<double>(green[neighborIndex]);

                    const double db =
                        static_cast<double>(blue[index]) -
                        static_cast<double>(blue[neighborIndex]);

                    const double rgbDistanceSquared =
                        dr * dr + dg * dg + db * db;

                    const float weight =
                        static_cast<float>(
                            weightScale *
                            inverseDistance[n] *
                            std::exp(
                                -std::pow(
                                    beta * rgbDistanceSquared,
                                    0.5
                                )
                            )
                        );

                    graph_->addEdge(
                        node,
                        neighborNode,
                        weight,
                        weight
                    );

                    ++created;
                }
            }
        }
    }

    neighborLinkCount_ = created;
    return created;
}

float TiledRefinement::calculateMinimumCut()
{
    if (!graph_) {
        return 0.0f;
    }

    return graph_->calculateMinimumCut();
}

void TiledRefinement::readSegmentationMask(
    Byte* segmentationMask,
    std::size_t size
) const
{
    if (!graph_ || !segmentationMask) {
        return;
    }

    const std::size_t required =
        static_cast<std::size_t>(width_) *
        static_cast<std::size_t>(height_);

    if (size < required) {
        return;
    }

    // Adobe's ReadSegmentationMask only visits active tiled
    // ImageGraphs. A sentinel node causes no write, preserving the
    // first-stage segmentation already present in the destination.
    int preReadbackMismatchCount = 0;

    for (const Tile& tile : tiles_) {
        if (!tile.active) {
            continue;
        }

        const int tileWidth = tile.right - tile.left;
        const int tileHeight = tile.bottom - tile.top;

        if (tileWidth <= 0 || tileHeight <= 0) {
            continue;
        }

        for (int localY = 0; localY < tileHeight; ++localY) {
            const int y = tile.top + localY;

            for (int localX = 0; localX < tileWidth; ++localX) {
                const int x = tile.left + localX;

                const std::size_t localIndex =
                    static_cast<std::size_t>(localY) *
                        static_cast<std::size_t>(tileWidth) +
                    static_cast<std::size_t>(localX);

                if (localIndex >= tile.nodeMap.size()) {
                    continue;
                }

                const int node = tile.nodeMap[localIndex];

                if (node == kNoNode) {
                    continue;
                }

                const std::size_t imageIndex =
                    static_cast<std::size_t>(y) *
                        static_cast<std::size_t>(width_) +
                    static_cast<std::size_t>(x);

                const Byte graphLabel =
                    graph_->isSourceTree(node)
                        ? static_cast<Byte>(0xff)
                        : static_cast<Byte>(0x00);

                if (segmentationMask[imageIndex] != graphLabel) {
                    ++preReadbackMismatchCount;
                }

                segmentationMask[imageIndex] = graphLabel;
            }
        }
    }

    preReadbackMismatchCount_ = preReadbackMismatchCount;
}

int TiledRefinement::activeTileCount() const noexcept
{
    int count = 0;

    for (const Tile& tile : tiles_) {
        if (tile.active) {
            ++count;
        }
    }

    return count;
}

} // namespace pstouch::quickselection::detail
