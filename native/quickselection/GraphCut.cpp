#include "GraphCut.hpp"

#include <algorithm>
#include <cmath>
#include <limits>
#include <queue>
#include <stdexcept>
#include <vector>

namespace pstouch::quickselection {

namespace {

struct Edge {
    int to;
    int reverse;

    // Current residual capacity used by the max-flow solver.
    double capacity;

    // Capacity assigned when this directed edge was created.
    // Keeping this separately lets the persistent Quick Selection graph
    // distinguish its energy definition from post-cut residual state.
    double originalCapacity;
};

struct EdgeHandle {
    int from;
    int index;
};

class FlowGraph {
public:
    explicit FlowGraph(int nodeCount)
        : graph_(static_cast<std::size_t>(nodeCount)),
          level_(static_cast<std::size_t>(nodeCount)),
          next_(static_cast<std::size_t>(nodeCount))
    {
    }

    EdgeHandle addEdge(int from, int to, double capacity)
    {
        const int forwardIndex =
            static_cast<int>(
                graph_[static_cast<std::size_t>(from)].size()
            );

        Edge forward{
            to,
            static_cast<int>(graph_[static_cast<std::size_t>(to)].size()),
            capacity,
            capacity
        };

        Edge reverse{
            from,
            forwardIndex,
            0.0,
            0.0
        };

        graph_[static_cast<std::size_t>(from)].push_back(forward);
        graph_[static_cast<std::size_t>(to)].push_back(reverse);

        return EdgeHandle{from, forwardIndex};
    }

    void addUndirectedEdge(int a, int b, double capacity)
    {
        (void)addEdge(a, b, capacity);
        (void)addEdge(b, a, capacity);
    }

    void updateEdgeCapacity(
        const EdgeHandle& handle,
        double newCapacity
    )
    {
        Edge& forward =
            graph_[static_cast<std::size_t>(handle.from)]
                  [static_cast<std::size_t>(handle.index)];

        Edge& reverse =
            graph_[static_cast<std::size_t>(forward.to)]
                  [static_cast<std::size_t>(forward.reverse)];

        // The reverse residual records the amount of flow currently
        // carried by this directed edge.
        const double oldFlow = reverse.capacity;

        // If the new capacity is smaller than the existing flow, that
        // flow cannot remain feasible on this edge. Clamp it for now.
        //
        // This gives us a well-defined persistent-capacity primitive.
        // Adobe's update_tweights behavior will be layered on top of
        // this rather than rebuilding the complete graph.
        const double retainedFlow =
            oldFlow < newCapacity ? oldFlow : newCapacity;

        forward.originalCapacity = newCapacity;
        forward.capacity = newCapacity - retainedFlow;

        reverse.capacity = retainedFlow;
    }

    double maxFlow(int source, int sink)
    {
        double total = 0.0;

        while (buildLevels(source, sink)) {
            std::fill(next_.begin(), next_.end(), 0);

            while (true) {
                const double flow = sendFlow(
                    source,
                    sink,
                    std::numeric_limits<double>::infinity()
                );

                if (flow <= epsilon()) {
                    break;
                }

                total += flow;
            }
        }

        return total;
    }

    std::vector<bool> sourceReachable(int source) const
    {
        std::vector<bool> seen(graph_.size(), false);
        std::queue<int> queue;

        seen[static_cast<std::size_t>(source)] = true;
        queue.push(source);

        while (!queue.empty()) {
            const int node = queue.front();
            queue.pop();

            for (const Edge& edge :
                 graph_[static_cast<std::size_t>(node)]) {
                if (edge.capacity > epsilon() &&
                    !seen[static_cast<std::size_t>(edge.to)]) {
                    seen[static_cast<std::size_t>(edge.to)] = true;
                    queue.push(edge.to);
                }
            }
        }

        return seen;
    }

    static bool testPersistentCapacityUpdate()
    {
        FlowGraph graph(2);

        const EdgeHandle edge = graph.addEdge(0, 1, 10.0);

        // Simulate 4 units of flow:
        // forward residual = 6, reverse residual = 4.
        Edge& forward =
            graph.graph_[static_cast<std::size_t>(edge.from)]
                        [static_cast<std::size_t>(edge.index)];

        Edge& reverse =
            graph.graph_[static_cast<std::size_t>(forward.to)]
                        [static_cast<std::size_t>(forward.reverse)];

        forward.capacity = 6.0;
        reverse.capacity = 4.0;

        // Increase 10 -> 15. Existing flow 4 must survive.
        graph.updateEdgeCapacity(edge, 15.0);

        if (forward.originalCapacity != 15.0 ||
            forward.capacity != 11.0 ||
            reverse.capacity != 4.0) {
            return false;
        }

        // Decrease 15 -> 3. Existing flow 4 no longer fits,
        // so this primitive clamps it to 3.
        graph.updateEdgeCapacity(edge, 3.0);

        if (forward.originalCapacity != 3.0 ||
            forward.capacity != 0.0 ||
            reverse.capacity != 3.0) {
            return false;
        }

        return true;
    }

private:
    static constexpr double epsilon() noexcept
    {
        return 1.0e-12;
    }

    bool buildLevels(int source, int sink)
    {
        std::fill(level_.begin(), level_.end(), -1);

        std::queue<int> queue;
        level_[static_cast<std::size_t>(source)] = 0;
        queue.push(source);

        while (!queue.empty()) {
            const int node = queue.front();
            queue.pop();

            for (const Edge& edge :
                 graph_[static_cast<std::size_t>(node)]) {
                if (edge.capacity > epsilon() &&
                    level_[static_cast<std::size_t>(edge.to)] < 0) {
                    level_[static_cast<std::size_t>(edge.to)] =
                        level_[static_cast<std::size_t>(node)] + 1;
                    queue.push(edge.to);
                }
            }
        }

        return level_[static_cast<std::size_t>(sink)] >= 0;
    }

    double sendFlow(int node, int sink, double flow)
    {
        if (node == sink) {
            return flow;
        }

        auto& edges = graph_[static_cast<std::size_t>(node)];

        for (std::size_t& i = next_[static_cast<std::size_t>(node)];
             i < edges.size();
             ++i) {
            Edge& edge = edges[i];

            if (edge.capacity <= epsilon()) {
                continue;
            }

            if (level_[static_cast<std::size_t>(edge.to)] !=
                level_[static_cast<std::size_t>(node)] + 1) {
                continue;
            }

            const double pushed = sendFlow(
                edge.to,
                sink,
                std::min(flow, edge.capacity)
            );

            if (pushed <= epsilon()) {
                continue;
            }

            edge.capacity -= pushed;

            Edge& reverse =
                graph_[static_cast<std::size_t>(edge.to)]
                      [static_cast<std::size_t>(edge.reverse)];

            reverse.capacity += pushed;

            return pushed;
        }

        return 0.0;
    }

    std::vector<std::vector<Edge>> graph_;
    std::vector<int> level_;
    std::vector<std::size_t> next_;
};

double colorDistanceSquared(
    const GraphCut::Byte* image,
    std::size_t a,
    std::size_t b
)
{
    const std::size_t ia = a * 3;
    const std::size_t ib = b * 3;

    double result = 0.0;

    for (std::size_t c = 0; c < 3; ++c) {
        const double delta =
            static_cast<double>(image[ia + c]) -
            static_cast<double>(image[ib + c]);

        result += delta * delta;
    }

    return result;
}

} // namespace

double GraphCut::adobeNeighborWeight(
    double rgbDistanceSquared
)
{
    // Adobe ARMv7 QuickSelect n-link:
    //
    // strength = 500
    // sigma = 16
    // beta = 0.5 / (16 * 16) = 1 / 512
    // exponent = 0.5
    //
    // w = 500 * exp(-sqrt(RGB_distance_squared / 512))
    constexpr double neighborStrength = 500.0;
    constexpr double rgbNormalization = 512.0;

    return neighborStrength *
           std::exp(
               -std::sqrt(
                   rgbDistanceSquared /
                   rgbNormalization
               )
           );
}


struct GraphCut::Impl {
    // Persistent graph state owned for the lifetime of GraphCut.
    std::unique_ptr<FlowGraph> graph;

    int width = 0;
    int height = 0;
    int source = -1;
    int sink = -1;

    // Stable handles for Adobe terminal 0 and terminal 1 edges.
    // Indexed by image pixel.
    std::vector<EdgeHandle> terminal0Edges;
    std::vector<EdgeHandle> terminal1Edges;

    bool initialized = false;

    void clear()
    {
        graph.reset();

        width = 0;
        height = 0;
        source = -1;
        sink = -1;

        terminal0Edges.clear();
        terminal1Edges.clear();

        initialized = false;
    }
};

GraphCut::GraphCut()
    : impl_(std::make_unique<Impl>())
{
}

GraphCut::~GraphCut() = default;

GraphCut::GraphCut(GraphCut&&) noexcept = default;

GraphCut& GraphCut::operator=(GraphCut&&) noexcept = default;

void GraphCut::reset()
{
    impl_->clear();
}

bool GraphCut::testPersistentCapacityUpdate()
{
    return FlowGraph::testPersistentCapacityUpdate();
}

GraphCut::TerminalWeights GraphCut::adobeTerminalWeights(
    bool foregroundConstraint,
    bool backgroundConstraint
)
{
    // Recovered from Adobe ARMv7:
    //
    // QuickSelect calls SetLocalEnergies with:
    //
    //   param_6 = 0.7
    //   param_7 = 1
    //   param_8 = 1
    //
    // Default:
    //
    //   terminal0 = 0.7 * 6.234410762786865
    //   terminal1 = 0
    //
    // Foreground scribble (+0x74):
    //   terminal0 = 0
    //   terminal1 = HUGE
    //
    // Background scribble (+0x78):
    //   terminal0 = HUGE
    //   terminal1 = 0
    //
    // Adobe ARMv7 QuickSelect passes param_14 = 5 to
    // ImageGraph::BuildNodesSegmentation.
    //
    // BuildNodesSegmentation stores:
    //   ImageGraph + 0x54 = ((2 * param_14) + 1)^2
    //                     = 11^2
    //                     = 121
    //
    // SetLocalEnergies then computes HUGE as:
    //   FLT_MAX / (2 * 121)
    //   = FLT_MAX / 242
    constexpr double localEnergyScale =
        6.234410762786865;

    constexpr double localEnergyParameter =
        0.7;

    constexpr double hardConstraint =
        static_cast<double>(std::numeric_limits<float>::max()) / 242.0;

    if (foregroundConstraint) {
        return {0.0, hardConstraint};
    }

    if (backgroundConstraint) {
        return {hardConstraint, 0.0};
    }

    return {
        localEnergyParameter * localEnergyScale,
        0.0
    };
}

void GraphCut::segment(
    const Byte* imageRGB,
    int width,
    int height,
    const Byte* foregroundConstraints,
    const Byte* backgroundConstraints,
    std::vector<Byte>& outputMask
)
{
    if (!imageRGB ||
        !foregroundConstraints ||
        !backgroundConstraints ||
        width <= 0 ||
        height <= 0) {
        throw std::invalid_argument("GraphCut: invalid input");
    }

    const std::size_t pixelCount =
        static_cast<std::size_t>(width) *
        static_cast<std::size_t>(height);

    if (pixelCount >
        static_cast<std::size_t>(std::numeric_limits<int>::max() - 2)) {
        throw std::overflow_error("GraphCut: image too large");
    }

    const int source = static_cast<int>(pixelCount);
    const int sink = source + 1;

    const bool canReuse =
        impl_->initialized &&
        impl_->graph &&
        impl_->width == width &&
        impl_->height == height &&
        impl_->source == source &&
        impl_->sink == sink &&
        impl_->terminal0Edges.size() == pixelCount &&
        impl_->terminal1Edges.size() == pixelCount;

    if (!canReuse) {
        // Adobe baseline lifecycle:
        //
        // Create / ReBuildNodesFromSegmentation
        //   -> SetLocalEnergies
        //   -> SetNeighborEnergies
        //
        // Build topology and energy edges once.
        impl_->clear();

        impl_->width = width;
        impl_->height = height;
        impl_->source = source;
        impl_->sink = sink;

        impl_->graph = std::make_unique<FlowGraph>(sink + 1);
        impl_->terminal0Edges.resize(pixelCount);
        impl_->terminal1Edges.resize(pixelCount);

        FlowGraph& graph = *impl_->graph;

        auto neighborWeight =
            [&](std::size_t a, std::size_t b) -> double
        {
            return GraphCut::adobeNeighborWeight(
                colorDistanceSquared(imageRGB, a, b)
            );
        };

        auto node = [width](int x, int y) -> int {
            return y * width + x;
        };

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                const int current = node(x, y);
                const std::size_t index =
                    static_cast<std::size_t>(current);

                const TerminalWeights terminal =
                    adobeTerminalWeights(
                        foregroundConstraints[index] != 0,
                        backgroundConstraints[index] != 0
                    );

                // Adobe terminal 0 = background.
                // Adobe terminal 1 = foreground.
                //
                // Our source-reachable class is foreground:
                //
                // terminal0 -> pixel -> sink
                // terminal1 -> source -> pixel
                impl_->terminal0Edges[index] =
                    graph.addEdge(
                        current,
                        sink,
                        terminal.terminal0
                    );

                impl_->terminal1Edges[index] =
                    graph.addEdge(
                        source,
                        current,
                        terminal.terminal1
                    );

                if (x + 1 < width) {
                    const int right = node(x + 1, y);

                    const double weight =
                        neighborWeight(
                            index,
                            static_cast<std::size_t>(right)
                        );

                    graph.addUndirectedEdge(
                        current,
                        right,
                        weight
                    );
                }

                if (y + 1 < height) {
                    const int below = node(x, y + 1);

                    const double weight =
                        neighborWeight(
                            index,
                            static_cast<std::size_t>(below)
                        );

                    graph.addUndirectedEdge(
                        current,
                        below,
                        weight
                    );
                }
            }
        }

        impl_->initialized = true;
    }
    else {
        // Adobe BrushHits uses SetFrozenEnergies on the existing
        // ImageGraph. Stage 1 updates all terminal pairs; affected-region
        // tracking comes next.
        FlowGraph& graph = *impl_->graph;

        for (std::size_t index = 0; index < pixelCount; ++index) {
            const TerminalWeights terminal =
                adobeTerminalWeights(
                    foregroundConstraints[index] != 0,
                    backgroundConstraints[index] != 0
                );

            graph.updateEdgeCapacity(
                impl_->terminal0Edges[index],
                terminal.terminal0
            );

            graph.updateEdgeCapacity(
                impl_->terminal1Edges[index],
                terminal.terminal1
            );
        }
    }

    FlowGraph& graph = *impl_->graph;

    // CalculateMinimumCut on the retained graph/residual network.
    graph.maxFlow(impl_->source, impl_->sink);

    const std::vector<bool> foreground =
        graph.sourceReachable(impl_->source);

    outputMask.resize(pixelCount);

    for (std::size_t i = 0; i < pixelCount; ++i) {
        outputMask[i] = foreground[i] ? 0xFF : 0x00;
    }
}

} // namespace pstouch::quickselection
