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
    double capacity;
};

class FlowGraph {
public:
    explicit FlowGraph(int nodeCount)
        : graph_(static_cast<std::size_t>(nodeCount)),
          level_(static_cast<std::size_t>(nodeCount)),
          next_(static_cast<std::size_t>(nodeCount))
    {
    }

    void addEdge(int from, int to, double capacity)
    {
        Edge forward{
            to,
            static_cast<int>(graph_[static_cast<std::size_t>(to)].size()),
            capacity
        };

        Edge reverse{
            from,
            static_cast<int>(graph_[static_cast<std::size_t>(from)].size()),
            0.0
        };

        graph_[static_cast<std::size_t>(from)].push_back(forward);
        graph_[static_cast<std::size_t>(to)].push_back(reverse);
    }

    void addUndirectedEdge(int a, int b, double capacity)
    {
        addEdge(a, b, capacity);
        addEdge(b, a, capacity);
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

    FlowGraph graph(sink + 1);

    // Hard scribble constraints must dominate all ordinary image edges.
    constexpr double hardConstraint = 1.0e12;

    // Adobe Photoshop Touch QuickSelect neighbor-energy model.
    //
    // Recovered from:
    //   ImageGraph::SetNeighborEnergiesRGBType<unsigned char>()
    //   ImageGraph::SetNeighborInverseDistances()
    //   ImageGraph::Create()
    //
    // QuickSelect configures neighborhood type 4:
    // horizontal + vertical edges only.
    //
    // Adobe parameters:
    //   strength = 500.0
    //   sigma input = 16.0 / 255.0
    //   sigma after internal *255 = 16.0
    //   beta = 0.5 / (16 * 16) = 1 / 512
    //   exponent = 0.5
    //
    // Therefore:
    //
    //   weight = 500 * exp(-sqrt(RGB_distance_squared / 512))
    //
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

            if (foregroundConstraints[index] != 0) {
                // Foreground is terminal/class 1 and must become FF.
                graph.addEdge(source, current, hardConstraint);
            }

            if (backgroundConstraints[index] != 0) {
                // Background is terminal/class 0 and must become 00.
                graph.addEdge(current, sink, hardConstraint);
            }

            if (x + 1 < width) {
                const int right = node(x + 1, y);

                const double weight =
                    neighborWeight(
                        index,
                        static_cast<std::size_t>(right)
                    );

                graph.addUndirectedEdge(current, right, weight);
            }

            if (y + 1 < height) {
                const int below = node(x, y + 1);

                const double weight =
                    neighborWeight(
                        index,
                        static_cast<std::size_t>(below)
                    );

                graph.addUndirectedEdge(current, below, weight);
            }
        }
    }

    graph.maxFlow(source, sink);

    const std::vector<bool> foreground =
        graph.sourceReachable(source);

    outputMask.resize(pixelCount);

    for (std::size_t i = 0; i < pixelCount; ++i) {
        // Recovered Adobe output contract:
        //
        // segmentation class 1 (foreground) -> FF
        // segmentation class 0 (background) -> 00
        outputMask[i] = foreground[i] ? 0xFF : 0x00;
    }
}

} // namespace pstouch::quickselection
