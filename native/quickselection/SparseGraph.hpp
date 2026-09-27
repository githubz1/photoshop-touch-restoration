#pragma once

#include <array>
#include <cstddef>
#include <memory>

namespace pstouch::quickselection::detail {

class SparseGraph {
public:
    virtual ~SparseGraph() = default;

    virtual std::size_t nodeCount() const noexcept = 0;
    virtual int addNode() = 0;

    virtual void addEdge(
        int from,
        int to,
        float forwardCapacity,
        float reverseCapacity
    ) = 0;

    virtual void setTerminals(int source, int sink) = 0;

    virtual void updateTweights(
        int node,
        float sourceCapacity,
        float sinkCapacity
    ) = 0;

    virtual float calculateMinimumCut() = 0;
    virtual bool isSourceTree(int node) const = 0;

    // Diagnostic only:
    // [0] SOURCE -> node assigned capacity
    // [1] SOURCE -> node residual
    // [2] node -> SINK assigned capacity
    // [3] node -> SINK residual
    virtual std::array<float, 4> diagnosticTerminalState(
        int node
    ) const = 0;
};

std::unique_ptr<SparseGraph> makeSparseGraph(std::size_t initialNodeCount);

} // namespace pstouch::quickselection::detail
