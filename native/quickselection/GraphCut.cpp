#include <list>
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

    void updateAdobeTerminalCapacity(
        const EdgeHandle& handle,
        double newCapacity
    )
    {
        Edge& forward =
            graph_[static_cast<std::size_t>(handle.from)]
                  [static_cast<std::size_t>(handle.index)];

        // Adobe SetFrozenEnergies:
        //
        //     delta = newCapacity - oldCapacity
        //     oldCapacity = newCapacity
        //     residual = max(0, residual + delta)
        //
        // This operates on the retained residual graph.
        const double delta =
            newCapacity - forward.originalCapacity;

        forward.originalCapacity = newCapacity;

        forward.capacity =
            std::max(
                0.0,
                forward.capacity + delta
            );
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

        const EdgeHandle edge =
            graph.addEdge(0, 1, 10.0);

        Edge& forward =
            graph.graph_[static_cast<std::size_t>(edge.from)]
                        [static_cast<std::size_t>(edge.index)];

        Edge& reverse =
            graph.graph_[static_cast<std::size_t>(forward.to)]
                        [static_cast<std::size_t>(forward.reverse)];

        // Existing Adobe state:
        //
        // defined capacity = 10
        // forward residual = 6
        // reverse residual = 4
        forward.capacity = 6.0;
        reverse.capacity = 4.0;

        // 10 -> 15:
        //
        // delta = +5
        // residual = 6 + 5 = 11
        graph.updateAdobeTerminalCapacity(
            edge,
            15.0
        );

        if (forward.originalCapacity != 15.0 ||
            forward.capacity != 11.0 ||
            reverse.capacity != 4.0) {
            return false;
        }

        // 15 -> 3:
        //
        // delta = -12
        // residual = max(0, 11 - 12) = 0
        graph.updateAdobeTerminalCapacity(
            edge,
            3.0
        );

        if (forward.originalCapacity != 3.0 ||
            forward.capacity != 0.0 ||
            reverse.capacity != 4.0) {
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


//
// Adobe Quick Selection persistent Boykov-Kolmogorov state.
//
// This is intentionally separate from FlowGraph while the recovered ARMv7
// grow/augment/adopt/update behavior is reconstructed. FlowGraph remains the
// reference backend until the BK implementation is complete.
//
class AdobeBKGraph {
public:
    enum class Tree : unsigned char {
        Source = 0,
        Sink   = 1,
        Free   = 2
    };

    struct Edge {
        int from = -1;
        int to = -1;
        int reverse = -1;

        // Adobe Edge +0x0c: residual capacity.
        float residual = 0.0f;

        // Logical capacity assigned to this directed edge. This is retained
        // for terminal-capacity delta updates.
        float assignedCapacity = 0.0f;
    };

    struct EdgeHandle {
        int from = -1;
        int index = -1;

        bool valid() const noexcept
        {
            return from >= 0 && index >= 0;
        }
    };

    struct NodeState {
        Tree tree = Tree::Free;

        // Adobe Kolmogorov +0x40 parent-edge array.
        EdgeHandle parentEdge{};
        bool orphanParent = false;

        // Adobe +0x68 per-node pair.
        std::uint32_t distance = 0;
        std::uint32_t timestamp = 0;

        bool active = false;
        bool orphan = false;
    };

    explicit AdobeBKGraph(std::size_t nodeCount)
        : adjacency_(nodeCount),
          nodes_(nodeCount)
    {
    }

    std::size_t nodeCount() const noexcept
    {
        return nodes_.size();
    }

    float flow() const noexcept
    {
        return flow_;
    }

    std::uint32_t timestamp() const noexcept
    {
        return timestamp_;
    }

    EdgeHandle addEdge(
        int from,
        int to,
        float forwardCapacity,
        float reverseCapacity
    )
    {
        if (from < 0 || to < 0 ||
            static_cast<std::size_t>(from) >= adjacency_.size() ||
            static_cast<std::size_t>(to) >= adjacency_.size()) {
            throw std::out_of_range("AdobeBKGraph::addEdge node");
        }

        const int forwardIndex =
            static_cast<int>(adjacency_[static_cast<std::size_t>(from)].size());

        const int reverseIndex =
            static_cast<int>(adjacency_[static_cast<std::size_t>(to)].size());

        adjacency_[static_cast<std::size_t>(from)].push_back(
            Edge{
                from,
                to,
                reverseIndex,
                forwardCapacity,
                forwardCapacity
            }
        );

        adjacency_[static_cast<std::size_t>(to)].push_back(
            Edge{
                to,
                from,
                forwardIndex,
                reverseCapacity,
                reverseCapacity
            }
        );

        return EdgeHandle{from, forwardIndex};
    }

    Edge& edge(const EdgeHandle& handle)
    {
        if (!handle.valid() ||
            static_cast<std::size_t>(handle.from) >= adjacency_.size() ||
            static_cast<std::size_t>(handle.index) >=
                adjacency_[static_cast<std::size_t>(handle.from)].size()) {
            throw std::out_of_range("AdobeBKGraph::edge handle");
        }

        return adjacency_[static_cast<std::size_t>(handle.from)]
                         [static_cast<std::size_t>(handle.index)];
    }

    const Edge& edge(const EdgeHandle& handle) const
    {
        if (!handle.valid() ||
            static_cast<std::size_t>(handle.from) >= adjacency_.size() ||
            static_cast<std::size_t>(handle.index) >=
                adjacency_[static_cast<std::size_t>(handle.from)].size()) {
            throw std::out_of_range("AdobeBKGraph::edge handle");
        }

        return adjacency_[static_cast<std::size_t>(handle.from)]
                         [static_cast<std::size_t>(handle.index)];
    }

    Edge& reverseEdge(const EdgeHandle& handle)
    {
        Edge& forward = edge(handle);

        return adjacency_[static_cast<std::size_t>(forward.to)]
                         [static_cast<std::size_t>(forward.reverse)];
    }

    const Edge& reverseEdge(const EdgeHandle& handle) const
    {
        const Edge& forward = edge(handle);

        return adjacency_[static_cast<std::size_t>(forward.to)]
                         [static_cast<std::size_t>(forward.reverse)];
    }

    void setTerminals(int sourceTerminal, int sinkTerminal)
    {
        if (sourceTerminal < 0 || sinkTerminal < 0 ||
            static_cast<std::size_t>(sourceTerminal) >= nodes_.size() ||
            static_cast<std::size_t>(sinkTerminal) >= nodes_.size() ||
            sourceTerminal == sinkTerminal) {
            throw std::out_of_range("AdobeBKGraph::setTerminals");
        }

        sourceTerminal_ = sourceTerminal;
        sinkTerminal_ = sinkTerminal;
    }

    float run()
    {
        // Adobe Kolmogorov::run():
        //
        // The constructor establishes the initial terminal state.
        // process_length_two_paths() then reduces all direct
        // SOURCE -> node -> SINK paths and constructs the initial forest.
        initializeSolverState();
        processLengthTwoPaths();

        // Unlike update(), the initial run does NOT call adopt()
        // before its first grow().
        if (grow()) {
            do {
                // Adobe increments +0x64 immediately before augment().
                ++timestamp_;

                augment();
                adopt();
            }
            while (grow());
        }

        return flow_;
    }

    float update()
    {
        // Adobe Kolmogorov::update():
        //
        // Dynamic terminal-capacity changes have already repaired/queued
        // affected tree state before this entry point is called.
        //
        // Unlike run(), update() begins by advancing the timestamp and
        // adopting any pending orphans before attempting to grow.
        ++timestamp_;
        adopt();

        if (grow()) {
            do {
                ++timestamp_;

                augment();
                adopt();
            }
            while (grow());
        }

        return flow_;
    }

    void updateLengthTwoPath(
        EdgeHandle sourceReverseHandle,
        EdgeHandle sinkDirectHandle)
    {
        if (!sourceReverseHandle.valid() ||
            !sinkDirectHandle.valid()) {
            throw std::logic_error(
                "AdobeBKGraph::updateLengthTwoPath invalid edge handle"
            );
        }

        // Recovered Adobe Kolmogorov::update_length_two_path().
        //
        // sourceReverseHandle is the SOURCE -> node edge obtained by
        // reversing the node -> SOURCE terminal edge.
        //
        // sinkDirectHandle is the node -> SINK terminal edge.
        Edge& sourceEdge = edge(sourceReverseHandle);
        Edge& sinkEdge = edge(sinkDirectHandle);

        const int node = sourceEdge.to;

        if (sinkEdge.from != node) {
            throw std::logic_error(
                "AdobeBKGraph::updateLengthTwoPath terminal-edge mismatch"
            );
        }

        // Cancel any newly available SOURCE -> node -> SINK residual path.
        const float pushed =
            std::min(sourceEdge.residual, sinkEdge.residual);

        if (pushed > 0.0f) {
            sinkEdge.residual -= pushed;
            reverseEdge(sinkDirectHandle).residual += pushed;

            sourceEdge.residual -= pushed;
            reverseEdge(sourceReverseHandle).residual += pushed;

            flow_ += pushed;
        }

        NodeState& state = nodeState(node);

        // If either updated terminal edge was the node's parent and its
        // usable residual has just been exhausted, Adobe marks the node
        // orphan for the next adopt() pass.
        if (state.parentEdge.valid()) {
            if (state.parentEdge.from == sourceReverseHandle.from &&
                state.parentEdge.index == sourceReverseHandle.index &&
                sourceEdge.residual == 0.0f) {
                makeOrphan(node);
            }
            else if (
                state.parentEdge.from == sinkDirectHandle.from &&
                state.parentEdge.index == sinkDirectHandle.index &&
                sinkEdge.residual == 0.0f) {
                makeOrphan(node);
            }
        }

        if (state.tree == Tree::Source) {
            if (sinkEdge.residual > 0.0f) {
                activate(node);
            }

            return;
        }

        if (state.tree == Tree::Sink) {
            if (sourceEdge.residual > 0.0f) {
                activate(node);
            }

            return;
        }

        if (state.tree != Tree::Free) {
            return;
        }

        // SOURCE has precedence when both residuals are positive.
        if (sourceEdge.residual > 0.0f) {
            state.tree = Tree::Source;
            state.parentEdge = sourceReverseHandle;
            state.orphanParent = false;

            activate(node);

            state.distance = 1;
            state.timestamp = timestamp_;
            return;
        }

        if (sinkEdge.residual > 0.0f) {
            state.tree = Tree::Sink;
            state.parentEdge = sinkDirectHandle;
            state.orphanParent = false;

            activate(node);

            state.distance = 1;
            state.timestamp = timestamp_;
        }
    }

    void processLengthTwoPaths()
    {
        if (sourceTerminal_ < 0 || sinkTerminal_ < 0) {
            throw std::logic_error(
                "AdobeBKGraph::processLengthTwoPaths without terminals"
            );
        }

        // Adobe process_length_two_paths():
        //
        // Walk every SOURCE -> node terminal path and locate the
        // corresponding node -> SINK edge. Cancel the common residual
        // immediately and use the remaining terminal residual to seed
        // the initial BK search forest.
        auto& sourceAdj =
            adjacency_[static_cast<std::size_t>(sourceTerminal_)];

        for (int sourceIndex = 0;
             sourceIndex < static_cast<int>(sourceAdj.size());
             ++sourceIndex) {

            const EdgeHandle sourceHandle{
                sourceTerminal_,
                sourceIndex
            };

            Edge& sourceEdge = edge(sourceHandle);

            const int node = sourceEdge.to;

            // The terminal itself cannot be an image node.
            if (node == sourceTerminal_ ||
                node == sinkTerminal_) {
                continue;
            }

            EdgeHandle sinkHandle{};

            auto& nodeAdj =
                adjacency_[static_cast<std::size_t>(node)];

            for (int edgeIndex = 0;
                 edgeIndex < static_cast<int>(nodeAdj.size());
                 ++edgeIndex) {

                if (nodeAdj[static_cast<std::size_t>(edgeIndex)].to ==
                    sinkTerminal_) {
                    sinkHandle = EdgeHandle{node, edgeIndex};
                    break;
                }
            }

            if (!sinkHandle.valid()) {
                continue;
            }

            Edge& sinkEdge = edge(sinkHandle);

            const float sourceResidual = sourceEdge.residual;
            const float sinkResidual = sinkEdge.residual;

            // Exact Adobe two-terminal path cancellation.
            const float pushed =
                std::min(sourceResidual, sinkResidual);

            if (pushed > 0.0f) {
                sourceEdge.residual -= pushed;
                reverseEdge(sourceHandle).residual += pushed;

                sinkEdge.residual -= pushed;
                reverseEdge(sinkHandle).residual += pushed;

                flow_ += pushed;
            }

            NodeState& state = nodeState(node);

            // Adobe tests the remaining terminal residuals.
            //
            // SOURCE residual wins when positive. If SOURCE is exhausted,
            // a positive SINK residual seeds the sink tree. Equal terminal
            // capacities therefore cancel completely and leave the node
            // FREE.
            if (sourceEdge.residual > 0.0f) {
                state.tree = Tree::Source;

                // The constructor/grow representation stores the reverse
                // edge as the SOURCE-side parent.
                state.parentEdge =
                    EdgeHandle{
                        sourceEdge.to,
                        sourceEdge.reverse
                    };

                state.orphanParent = false;
                state.distance = 1;
                state.timestamp = timestamp_;

                activate(node);
            }
            else if (sinkEdge.residual > 0.0f) {
                state.tree = Tree::Sink;
                state.parentEdge = sinkHandle;
                state.orphanParent = false;
                state.distance = 1;
                state.timestamp = timestamp_;

                activate(node);
            }
        }

        // Adobe explicitly removes both terminal nodes from the active
        // list after constructing the initial forest.
        deactivate(sourceTerminal_);
        deactivate(sinkTerminal_);
    }

    void initializeSolverState()
    {
        if (sourceTerminal_ < 0 || sinkTerminal_ < 0) {
            throw std::logic_error(
                "AdobeBKGraph::initializeSolverState without terminals"
            );
        }

        active_.clear();
        orphans_.clear();
        terminalRecheck_.clear();

        bridge_ = {};
        flow_ = 0.0f;

        // Recovered Kolmogorov constructor:
        //
        //   +0x3c = Source = 0
        //   +0x3d = Sink   = 1
        //   +0x3e = Free   = 2
        //
        // Every node begins FREE with no parent and an invalid
        // distance/timestamp cache.
        for (std::size_t i = 0; i < nodes_.size(); ++i) {
            NodeState& state = nodes_[i];

            state.tree = Tree::Free;
            state.parentEdge = {};
            state.orphanParent = false;
            state.distance = 0;
            state.timestamp = 0;
            state.active = false;
            state.orphan = false;
        }

        // Adobe constructor inserts SINK first, then SOURCE into the
        // active list.
        activate(sinkTerminal_);
        activate(sourceTerminal_);

        NodeState& source = nodeState(sourceTerminal_);
        source.tree = Tree::Source;
        source.parentEdge = {};
        source.orphanParent = false;
        source.distance = 0;
        source.timestamp = 1;

        NodeState& sink = nodeState(sinkTerminal_);
        sink.tree = Tree::Sink;
        sink.parentEdge = {};
        sink.orphanParent = false;
        sink.distance = 0;
        sink.timestamp = 1;

        // Kolmogorov +0x64.
        timestamp_ = 1;
    }

    void activate(int node)
    {
        NodeState& state = nodeState(node);

        if (state.active) {
            return;
        }

        state.active = true;
        active_.push_back(node);
    }

    void deactivate(int node)
    {
        NodeState& state = nodeState(node);

        if (!state.active) {
            return;
        }

        state.active = false;

        for (auto it = active_.begin(); it != active_.end(); ++it) {
            if (*it == node) {
                active_.erase(it);
                break;
            }
        }
    }

    void makeOrphan(int node)
    {
        ++makeOrphanCallCount_;

        NodeState& state = nodeState(node);

        state.parentEdge = {};
        state.orphanParent = true;

        if (state.orphan) {
            return;
        }

        state.orphan = true;
        orphans_.push_back(node);
    }

    void initializeNode(
        int node,
        Tree tree,
        EdgeHandle parentEdge,
        std::uint32_t distance,
        std::uint32_t timestamp,
        bool active
    )
    {
        NodeState& state = nodeState(node);

        state.tree = tree;
        state.parentEdge = parentEdge;
        state.orphanParent = false;
        state.distance = distance;
        state.timestamp = timestamp;
        state.orphan = false;

        if (active) {
            activate(node);
        } else {
            state.active = false;
        }
    }

    bool grow()
    {
        bridge_ = {};

        while (!active_.empty()) {
            const int node = active_.front();
            NodeState& nodeInfo = nodeState(node);

            if (!nodeInfo.active) {
                active_.pop_front();
                continue;
            }

            auto& edges = adjacency_[static_cast<std::size_t>(node)];

            for (int edgeIndex = 0;
                 edgeIndex < static_cast<int>(edges.size());
                 ++edgeIndex) {

                const EdgeHandle handle{node, edgeIndex};
                Edge& forward = edge(handle);

                // Recovered Adobe grow():
                // SOURCE tree tests the forward residual.
                // SINK tree tests the reverse residual.
                Edge& usable =
                    nodeInfo.tree == Tree::Source
                        ? forward
                        : reverseEdge(handle);

                if (!(usable.residual > 0.0f)) {
                    continue;
                }

                const int neighbor = forward.to;
                NodeState& neighborInfo = nodeState(neighbor);

                if (neighborInfo.tree == Tree::Free) {
                    neighborInfo.tree = nodeInfo.tree;

                    // Adobe stores the reverse edge as the child's parent.
                    Edge& reverse = reverseEdge(handle);
                    neighborInfo.parentEdge =
                        EdgeHandle{reverse.from, forward.reverse};
                    neighborInfo.orphanParent = false;

                    activate(neighbor);

                    neighborInfo.distance = nodeInfo.distance + 1;
                    neighborInfo.timestamp = nodeInfo.timestamp;
                    continue;
                }

                if (neighborInfo.tree != nodeInfo.tree) {
                    // SOURCE and SINK trees have met.
                    bridge_ = handle;
                    return true;
                }

                // Exact Adobe grow(): same-tree improvement changes
                // only the parent edge. Distance/timestamp are not
                // rewritten in this branch.
                if (neighborInfo.timestamp <= nodeInfo.timestamp &&
                    nodeInfo.distance < neighborInfo.distance) {

                    Edge& reverse = reverseEdge(handle);
                    neighborInfo.parentEdge =
                        EdgeHandle{reverse.from, forward.reverse};
                    neighborInfo.orphanParent = false;
                }
            }

            deactivate(node);
        }

        return false;
    }

    static bool sameEdgeHandle(
        const EdgeHandle& a,
        const EdgeHandle& b
    ) noexcept
    {
        return a.from == b.from &&
               a.index == b.index;
    }

    bool originIsTerminal(
        int node,
        std::uint32_t& outDistance
    )
    {
        outDistance = 0;

        const Tree tree = nodeState(node).tree;

        if (tree == Tree::Free) {
            return false;
        }

        const int terminal =
            tree == Tree::Source
                ? sourceTerminal_
                : sinkTerminal_;

        int current = node;

        NodeState& initial = nodeState(current);

        if (initial.timestamp == timestamp_ ||
            current == terminal) {

            outDistance = initial.distance;
        }
        else {
            std::uint32_t traversed = 0;

            while (nodeState(current).timestamp != timestamp_ &&
                   current != terminal) {

                const NodeState& state = nodeState(current);

                if (state.orphanParent ||
                    !state.parentEdge.valid()) {
                    return false;
                }

                current = edge(state.parentEdge).to;
                ++traversed;
            }

            const int anchor = current;

            outDistance =
                nodeState(anchor).distance + traversed;

            std::uint32_t distance = outDistance;
            current = node;

            // Cache the validated distance for each node on the path.
            while (current != anchor) {
                NodeState& state = nodeState(current);

                state.distance = distance;
                state.timestamp = timestamp_;

                --distance;
                current = edge(state.parentEdge).to;
            }
        }

        if (current == terminal) {
            NodeState& terminalState = nodeState(current);

            terminalState.distance = 0;
            terminalState.timestamp = timestamp_;
        }

        return true;
    }

    float augment()
    {
        if (!bridge_.valid()) {
            throw std::logic_error("AdobeBKGraph::augment without bridge");
        }

        EdgeHandle bridge = bridge_;
        Edge* bridgeEdge = &edge(bridge);

        int sourceNode = bridge.from;
        int sinkNode = bridgeEdge->to;

        // Adobe orients +0x50 so that the bridge runs from the SOURCE
        // tree toward the SINK tree.
        if (nodeState(sinkNode).tree == Tree::Source) {
            Edge& reverse = reverseEdge(bridge);
            bridge = EdgeHandle{reverse.from, bridgeEdge->reverse};
            bridge_ = bridge;
            bridgeEdge = &edge(bridge);

            sourceNode = bridge.from;
            sinkNode = bridgeEdge->to;
        }

        float bottleneck = bridgeEdge->residual;

        // SOURCE side.
        //
        // Adobe walks parent[node] toward the source terminal and tests
        // the residual capacity of parent->reverse.
        for (int node = sourceNode; node != sourceTerminal_;) {
            const NodeState& state = nodeState(node);

            if (!state.parentEdge.valid() || state.orphanParent) {
                throw std::logic_error(
                    "AdobeBKGraph::augment invalid SOURCE parent"
                );
            }

            const Edge& parent = edge(state.parentEdge);
            const Edge& reverse = reverseEdge(state.parentEdge);

            if (reverse.residual < bottleneck) {
                bottleneck = reverse.residual;
            }

            node = parent.to;
        }

        // SINK side.
        //
        // Adobe walks parent[node] toward the sink terminal and tests
        // parent->residual directly.
        for (int node = sinkNode; node != sinkTerminal_;) {
            const NodeState& state = nodeState(node);

            if (!state.parentEdge.valid() || state.orphanParent) {
                throw std::logic_error(
                    "AdobeBKGraph::augment invalid SINK parent"
                );
            }

            const Edge& parent = edge(state.parentEdge);

            if (parent.residual < bottleneck) {
                bottleneck = parent.residual;
            }

            node = parent.to;
        }

        // Push through the SOURCE -> SINK bridge.
        bridgeEdge = &edge(bridge);
        Edge& bridgeReverse = reverseEdge(bridge);

        bridgeEdge->residual -= bottleneck;
        bridgeReverse.residual += bottleneck;

        // Push through the SOURCE tree.
        for (int node = sourceNode; node != sourceTerminal_;) {
            NodeState& state = nodeState(node);
            const EdgeHandle parentHandle = state.parentEdge;

            Edge& parent = edge(parentHandle);
            Edge& reverse = reverseEdge(parentHandle);

            parent.residual += bottleneck;
            reverse.residual -= bottleneck;

            const int nextNode = parent.to;

            // Exact Adobe behavior: direct float zero comparison.
            if (reverse.residual == 0.0f) {
                makeOrphan(node);
            }

            node = nextNode;
        }

        // Push through the SINK tree.
        for (int node = sinkNode; node != sinkTerminal_;) {
            NodeState& state = nodeState(node);
            const EdgeHandle parentHandle = state.parentEdge;

            Edge& parent = edge(parentHandle);
            Edge& reverse = reverseEdge(parentHandle);

            parent.residual -= bottleneck;
            reverse.residual += bottleneck;

            const int nextNode = parent.to;

            // Exact Adobe behavior: direct float zero comparison.
            if (parent.residual == 0.0f) {
                makeOrphan(node);
            }

            node = nextNode;
        }

        // Adobe Kolmogorov +0x18.
        flow_ += bottleneck;
        return flow_;
    }

    void adopt()
    {
        ++adoptCallCount_;

        while (!orphans_.empty()) {
            const int orphan = orphans_.front();
            orphans_.pop_front();

            NodeState& orphanState = nodeState(orphan);
            orphanState.orphan = false;

            const Tree orphanTree = orphanState.tree;

            if (orphanTree == Tree::Free) {
                continue;
            }

            EdgeHandle bestParent{};
            std::uint32_t bestDistance =
                std::numeric_limits<std::uint32_t>::max();

            // Search all adjacent nodes for the nearest valid parent
            // belonging to the same tree.
            auto& edges = adjacency_[static_cast<std::size_t>(orphan)];

            for (int edgeIndex = 0;
                 edgeIndex < static_cast<int>(edges.size());
                 ++edgeIndex) {

                const EdgeHandle outgoing{orphan, edgeIndex};
                Edge& edgeToNeighbor = edge(outgoing);

                const int neighbor = edgeToNeighbor.to;
                NodeState& neighborState = nodeState(neighbor);

                if (neighborState.tree != orphanTree) {
                    continue;
                }

                // Adobe chooses residual direction according to tree:
                //
                // SOURCE orphan: neighbor -> orphan must have residual.
                // SINK orphan:   orphan -> neighbor must have residual.
                const float usableResidual =
                    orphanTree == Tree::Source
                        ? reverseEdge(outgoing).residual
                        : edgeToNeighbor.residual;

                if (!(usableResidual > 0.0f)) {
                    continue;
                }

                std::uint32_t distance = 0;

                if (!originIsTerminal(neighbor, distance)) {
                    continue;
                }

                if (distance < bestDistance) {
                    bestDistance = distance;

                    // Parent edges point from child toward parent.
                    bestParent = outgoing;
                }
            }

            if (bestParent.valid()) {
                orphanState.parentEdge = bestParent;
                orphanState.orphanParent = false;
                orphanState.distance = bestDistance + 1;
                orphanState.timestamp = timestamp_;
                continue;
            }

            // No valid parent exists. Adobe now detaches this node and
            // examines same-tree neighbors for activation and orphaning.
            for (int edgeIndex = 0;
                 edgeIndex < static_cast<int>(edges.size());
                 ++edgeIndex) {

                const EdgeHandle outgoing{orphan, edgeIndex};
                Edge& edgeToNeighbor = edge(outgoing);

                const int neighbor = edgeToNeighbor.to;
                NodeState& neighborState = nodeState(neighbor);

                if (neighborState.tree != orphanTree) {
                    continue;
                }

                const float usableResidual =
                    orphanTree == Tree::Source
                        ? reverseEdge(outgoing).residual
                        : edgeToNeighbor.residual;

                if (usableResidual > 0.0f) {
                    activate(neighbor);
                }

                // If neighbor's parent edge leads to this orphan,
                // neighbor becomes an orphan too.
                if (neighborState.parentEdge.valid() &&
                    !neighborState.orphanParent) {

                    const Edge& neighborParent =
                        edge(neighborState.parentEdge);

                    if (neighborParent.to == orphan) {
                        makeOrphan(neighbor);
                    }
                }
            }

            orphanState.tree = Tree::Free;
            orphanState.parentEdge = {};
            orphanState.orphanParent = false;

            deactivate(orphan);

            // Adobe stages detached nodes for the terminal-edge
            // recheck performed after orphan adoption.
            terminalRecheck_.push_back(orphan);
        }

        // Adobe adopt() second stage:
        // reconsider detached nodes against the two terminal edges.
        while (!terminalRecheck_.empty()) {
            const int node = terminalRecheck_.front();
            terminalRecheck_.pop_front();

            NodeState& state = nodeState(node);

            EdgeHandle sourceEdge{};
            EdgeHandle sinkEdge{};

            auto& edges =
                adjacency_[static_cast<std::size_t>(node)];

            for (int edgeIndex = 0;
                 edgeIndex < static_cast<int>(edges.size());
                 ++edgeIndex) {

                const EdgeHandle handle{node, edgeIndex};
                const Edge& candidate = edge(handle);

                if (candidate.to == sourceTerminal_) {
                    sourceEdge = handle;
                }
                else if (candidate.to == sinkTerminal_) {
                    sinkEdge = handle;
                }
            }

            // ARM 01000420 onward:
            //
            // For the source terminal Adobe takes the reverse of the
            // node -> source edge, tests that reverse residual, and
            // stores the reversed edge as the SOURCE-tree parent.
            if (sourceEdge.valid()) {
                const Edge& source = edge(sourceEdge);

                const EdgeHandle sourceReverse{
                    source.to,
                    source.reverse
                };

                if (edge(sourceReverse).residual > 0.0f) {
                    state.tree = Tree::Source;
                    state.parentEdge = sourceReverse;
                    state.orphanParent = false;

                    activate(node);

                    state.distance = 1;
                    state.timestamp = timestamp_;
                    continue;
                }
            }

            // Sink is considered only when the source-terminal residual
            // was not positive. Adobe stores this edge directly.
            if (sinkEdge.valid() &&
                edge(sinkEdge).residual > 0.0f) {

                state.tree = Tree::Sink;
                state.parentEdge = sinkEdge;
                state.orphanParent = false;

                activate(node);

                state.distance = 1;
                state.timestamp = timestamp_;
            }
        }
    }

    static void runRecoveredSolverSelfTest()
    {
        auto check = [](
            const char* name,
            float sourceCapacity,
            float sinkCapacity,
            float expectedFlow)
        {
            // Exact current AdobeBKGraph API:
            // node 0 = source
            // node 1 = pixel
            // node 2 = sink
            AdobeBKGraph graph(3);

            const int source = 0;
            const int pixel  = 1;
            const int sink   = 2;

            graph.setTerminals(source, sink);

            graph.addEdge(
                source,
                pixel,
                sourceCapacity,
                0.0f
            );

            graph.addEdge(
                pixel,
                sink,
                sinkCapacity,
                0.0f
            );

            const float actual = graph.run();

            if (std::fabs(actual - expectedFlow) > 1.0e-5f) {
                throw std::runtime_error(
                    std::string("AdobeBKGraph self-test failed: ") +
                    name +
                    " expected=" + std::to_string(expectedFlow) +
                    " actual=" + std::to_string(actual)
                );
            }
        };

        check(
            "balanced terminal path",
            10.0f,
            10.0f,
            10.0f
        );

        check(
            "sink bottleneck",
            10.0f,
            3.0f,
            3.0f
        );

        check(
            "source bottleneck",
            3.0f,
            10.0f,
            3.0f
        );


        // This path cannot be completed by processLengthTwoPaths().
        // It forces the recovered BK grow()/augment() path to execute.
        {
            AdobeBKGraph graph(4);

            const int source = 0;
            const int a      = 1;
            const int b      = 2;
            const int sink   = 3;

            graph.setTerminals(source, sink);

            // Adobe/ImageGraph topology gives every image node
            // both terminal connections, including zero-capacity ones.
            graph.addEdge(source, a, 10.0f, 0.0f);
            graph.addEdge(a, sink,    0.0f, 0.0f);

            graph.addEdge(source, b,  0.0f, 0.0f);
            graph.addEdge(b, sink,   10.0f, 0.0f);

            // N-link connecting the two image nodes.
            graph.addEdge(a, b,       7.0f, 7.0f);

            const float actual = graph.run();
            const float expected = 7.0f;

            if (std::fabs(actual - expected) > 1.0e-5f) {
                throw std::runtime_error(
                    std::string(
                        "AdobeBKGraph grow/augment self-test failed"
                    ) +
                    " expected=" + std::to_string(expected) +
                    " actual=" + std::to_string(actual)
                );
            }

            if (graph.adoptCallCount_ == 0) {
                throw std::runtime_error(
                    "AdobeBKGraph diagnostic failed: adopt() was not called"
                );
            }

        }

        // Deliberately saturate a SOURCE-tree parent edge.
        // processLengthTwoPaths() seeds A as SOURCE and B as SINK.
        // grow() finds A -> B.  The 4-unit SOURCE parent is the
        // bottleneck, so augment() must call makeOrphan(A).
        {
            AdobeBKGraph graph(4);

            const int source = 0;
            const int a = 1;
            const int b = 2;
            const int sink = 3;

            graph.setTerminals(source, sink);

            // Every image node has both Adobe terminal connections,
            // including zero-capacity counterparts.
            graph.addEdge(source, a, 4.0f, 0.0f);
            graph.addEdge(a, sink, 0.0f, 0.0f);

            graph.addEdge(source, b, 0.0f, 0.0f);
            graph.addEdge(b, sink, 10.0f, 0.0f);

            graph.addEdge(a, b, 10.0f, 10.0f);

            const float actual = graph.run();
            const float expected = 4.0f;

            if (std::fabs(actual - expected) > 1.0e-5f) {
                throw std::runtime_error(
                    std::string(
                        "AdobeBKGraph orphan self-test failed"
                    ) +
                    " expected=" + std::to_string(expected) +
                    " actual=" + std::to_string(actual)
                );
            }

            if (graph.makeOrphanCallCount_ == 0) {
                throw std::runtime_error(
                    "AdobeBKGraph orphan self-test failed: "
                    "makeOrphan() was not called"
                );
            }

            if (graph.adoptCallCount_ == 0) {
                throw std::runtime_error(
                    "AdobeBKGraph orphan self-test failed: "
                    "adopt() was not called"
                );
            }
        }

        // Dynamic update_length_two_path() regression.
        //
        // Begin with a completely saturated 5-unit terminal path. Then
        // expose three new residual units on both sides of the same image
        // node and invoke Adobe's recovered dynamic length-two-path repair.
        {
            AdobeBKGraph graph(3);

            const int source = 0;
            const int pixel = 1;
            const int sink = 2;

            graph.setTerminals(source, sink);

            const EdgeHandle sourceDirect =
                graph.addEdge(source, pixel, 5.0f, 0.0f);

            const EdgeHandle sinkDirect =
                graph.addEdge(pixel, sink, 5.0f, 0.0f);

            const float initialFlow = graph.run();

            if (std::fabs(initialFlow - 5.0f) > 1.0e-5f) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic length-two-path self-test: "
                    "initial flow mismatch"
                );
            }

            // Adobe update_length_two_path() receives the reversed
            // node->SOURCE terminal edge, i.e. SOURCE->node. In our graph
            // that is exactly sourceDirect.
            const EdgeHandle sourceTerminalEdge = sourceDirect;

            // Simulate the residual state presented to
            // update_length_two_path() after update_tweights().
            graph.edge(sourceTerminalEdge).residual = 3.0f;
            graph.edge(sinkDirect).residual = 3.0f;

            graph.updateLengthTwoPath(
                sourceTerminalEdge,
                sinkDirect
            );

            if (std::fabs(graph.flow_ - 8.0f) > 1.0e-5f) {
                throw std::runtime_error(
                    std::string(
                        "AdobeBKGraph dynamic length-two-path self-test: "
                        "expected flow=8 actual="
                    ) +
                    std::to_string(graph.flow_)
                );
            }

            if (graph.edge(sourceTerminalEdge).residual != 0.0f ||
                graph.edge(sinkDirect).residual != 0.0f) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic length-two-path self-test: "
                    "forward residuals were not cancelled"
                );
            }

            if (std::fabs(
                    graph.reverseEdge(sourceTerminalEdge).residual - 8.0f
                ) > 1.0e-5f ||
                std::fabs(
                    graph.reverseEdge(sinkDirect).residual - 8.0f
                ) > 1.0e-5f) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic length-two-path self-test: "
                    "reverse residuals were not updated"
                );
            }
        }


        // Dynamic orphan regression.
        //
        // Construct an existing SOURCE-tree pixel whose parent is the
        // SOURCE->pixel terminal edge.  update_length_two_path() then
        // consumes the last residual unit on that parent edge. Adobe must
        // immediately mark the pixel orphan; update() must subsequently
        // enter adopt() and repair the queued dynamic state.
        {
            AdobeBKGraph graph(3);

            const int source = 0;
            const int pixel = 1;
            const int sink = 2;

            graph.setTerminals(source, sink);

            const EdgeHandle sourceTerminalEdge =
                graph.addEdge(source, pixel, 1.0f, 0.0f);

            const EdgeHandle sinkDirect =
                graph.addEdge(pixel, sink, 1.0f, 0.0f);

            graph.initializeSolverState();

            graph.initializeNode(
                pixel,
                Tree::Source,
                sourceTerminalEdge,
                1,
                graph.timestamp_,
                false
            );

            const std::size_t orphanCallsBefore =
                graph.makeOrphanCallCount_;

            const std::size_t adoptCallsBefore =
                graph.adoptCallCount_;

            graph.updateLengthTwoPath(
                sourceTerminalEdge,
                sinkDirect
            );

            if (graph.makeOrphanCallCount_ !=
                orphanCallsBefore + 1) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic orphan self-test: "
                    "parent saturation did not create orphan"
                );
            }

            const NodeState& orphanState =
                graph.nodeState(pixel);

            if (!orphanState.orphan ||
                !orphanState.orphanParent ||
                orphanState.parentEdge.valid()) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic orphan self-test: "
                    "orphan state mismatch"
                );
            }

            if (std::fabs(graph.flow_ - 1.0f) > 1.0e-5f) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic orphan self-test: "
                    "length-two flow mismatch"
                );
            }

            const float updatedFlow = graph.update();

            if (std::fabs(updatedFlow - 1.0f) > 1.0e-5f) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic orphan self-test: "
                    "update changed completed flow"
                );
            }

            if (graph.adoptCallCount_ <= adoptCallsBefore) {
                throw std::runtime_error(
                    "AdobeBKGraph dynamic orphan self-test: "
                    "update did not enter adopt"
                );
            }
        }
    }

private:
    NodeState& nodeState(int node)
    {
        if (node < 0 ||
            static_cast<std::size_t>(node) >= nodes_.size()) {
            throw std::out_of_range("AdobeBKGraph node");
        }

        return nodes_[static_cast<std::size_t>(node)];
    }

    const NodeState& nodeState(int node) const
    {
        if (node < 0 ||
            static_cast<std::size_t>(node) >= nodes_.size()) {
            throw std::out_of_range("AdobeBKGraph node");
        }

        return nodes_[static_cast<std::size_t>(node)];
    }

    // Graph topology / residual network.
    std::vector<std::vector<Edge>> adjacency_;

    // Persistent BK tree state recovered from the ARMv7 solver.
    std::vector<NodeState> nodes_;

    // Adobe +0x2c active-node list.
    std::list<int> active_;

    // Adobe orphan processing performed by adopt().
    std::list<int> orphans_;

    // Detached nodes reconsidered against terminal edges after adoption.
    std::list<int> terminalRecheck_;

    // Adobe Kolmogorov +0x18.
    float flow_ = 0.0f;

    // Adobe Kolmogorov +0x64.
    std::uint32_t timestamp_ = 0;

    // Adobe Kolmogorov +0x50: edge joining source and sink trees.
    EdgeHandle bridge_{};

    // Terminal node IDs supplied to the Kolmogorov constructor.
    int sourceTerminal_ = -1;
    int sinkTerminal_ = -1;

    // Temporary recovered-solver diagnostics.
    std::uint32_t makeOrphanCallCount_ = 0;
    std::uint32_t adoptCallCount_ = 0;
};

namespace {
const bool adobeBKSelfTestPassed = []() {
    AdobeBKGraph::runRecoveredSolverSelfTest();
    return true;
}();
}

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

void GraphCut::initialize(
    const Byte* imageRGB,
    int width,
    int height,
    const Byte* foregroundConstraints,
    const Byte* backgroundConstraints
)
{
    if (!imageRGB ||
        !foregroundConstraints ||
        !backgroundConstraints ||
        width <= 0 ||
        height <= 0) {
        throw std::invalid_argument(
            "GraphCut::initialize received invalid input"
        );
    }

    impl_->clear();

    const std::size_t pixelCount =
        static_cast<std::size_t>(width) *
        static_cast<std::size_t>(height);

    const int source =
        static_cast<int>(pixelCount);

    const int sink =
        source + 1;

    impl_->width = width;
    impl_->height = height;
    impl_->source = source;
    impl_->sink = sink;

    impl_->graph =
        std::make_unique<FlowGraph>(sink + 1);

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
            const int current =
                node(x, y);

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

            // Exact active QuickSelect topology:
            // four-neighbor horizontal/vertical n-links.
            if (x + 1 < width) {
                const int right =
                    node(x + 1, y);

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
                const int below =
                    node(x, y + 1);

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


void GraphCut::updateFrozenEnergies(
    const Byte* foregroundConstraints,
    const Byte* backgroundConstraints,
    int x,
    int y,
    int width,
    int height
)
{
    if (!impl_->initialized || !impl_->graph) {
        throw std::logic_error(
            "GraphCut::updateFrozenEnergies called before initialize"
        );
    }

    if (!foregroundConstraints ||
        !backgroundConstraints) {
        throw std::invalid_argument(
            "GraphCut::updateFrozenEnergies received null mask"
        );
    }

    if (width <= 0 || height <= 0) {
        return;
    }

    // Clip the requested SetFrozenEnergies region to ImageGraph.
    const int minX =
        std::max(0, x);

    const int minY =
        std::max(0, y);

    const int maxX =
        std::min(impl_->width, x + width);

    const int maxY =
        std::min(impl_->height, y + height);

    if (minX >= maxX || minY >= maxY) {
        return;
    }

    FlowGraph& graph =
        *impl_->graph;

    for (int py = minY; py < maxY; ++py) {
        for (int px = minX; px < maxX; ++px) {
            const std::size_t index =
                static_cast<std::size_t>(py) *
                    static_cast<std::size_t>(impl_->width) +
                static_cast<std::size_t>(px);

            // Adobe ImageGraph::SetFrozenEnergies only touches a node
            // when one of the frozen masks exceeds the 0x7f threshold.
            // Pixels inside the update rectangle that are unconstrained
            // retain their existing terminal energies/residual state.
            const bool foreground =
                foregroundConstraints[index] > 0x7f;

            const bool background =
                backgroundConstraints[index] > 0x7f;

            if (!foreground && !background) {
                continue;
            }

            // Adobe gives the foreground mask precedence when both masks
            // are set for the same pixel.
            const TerminalWeights terminal =
                adobeTerminalWeights(
                    foreground,
                    foreground ? false : background
                );

            graph.updateAdobeTerminalCapacity(
                impl_->terminal0Edges[index],
                terminal.terminal0
            );

            graph.updateAdobeTerminalCapacity(
                impl_->terminal1Edges[index],
                terminal.terminal1
            );
        }
    }
}


void GraphCut::calculateMinimumCut(
    std::vector<Byte>& outputMask
)
{
    if (!impl_->initialized || !impl_->graph) {
        throw std::logic_error(
            "GraphCut::calculateMinimumCut called before initialize"
        );
    }

    FlowGraph& graph =
        *impl_->graph;

    // Adobe equivalent:
    // mincut_adjlist_graph<float>::CalculateMinimumCut()
    graph.maxFlow(
        impl_->source,
        impl_->sink
    );

    const std::vector<bool> foreground =
        graph.sourceReachable(
            impl_->source
        );

    const std::size_t pixelCount =
        static_cast<std::size_t>(impl_->width) *
        static_cast<std::size_t>(impl_->height);

    outputMask.resize(pixelCount);

    for (std::size_t i = 0; i < pixelCount; ++i) {
        outputMask[i] =
            foreground[i] ? 0xFF : 0x00;
    }
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
        throw std::invalid_argument(
            "GraphCut::segment received invalid input"
        );
    }

    const bool canReuse =
        impl_->initialized &&
        impl_->graph &&
        impl_->width == width &&
        impl_->height == height;

    if (!canReuse) {
        initialize(
            imageRGB,
            width,
            height,
            foregroundConstraints,
            backgroundConstraints
        );
    }
    else {
        // Compatibility behavior: update the complete ImageGraph.
        // QuickSelectTool will use updateFrozenEnergies() directly
        // with per-brush affected rectangles.
        updateFrozenEnergies(
            foregroundConstraints,
            backgroundConstraints,
            0,
            0,
            width,
            height
        );
    }

    calculateMinimumCut(outputMask);
}

} // namespace pstouch::quickselection
