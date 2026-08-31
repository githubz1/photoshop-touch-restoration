from __future__ import annotations

from phoenix.optimization.constants import ConstantPropagation
from phoenix.optimization.copyprop import CopyPropagation
from phoenix.optimization.deadstores import DeadStoreElimination


class OptimizationPipeline:
    """
    Runs optimization passes repeatedly until the AST reaches
    a fixed point or the iteration limit is reached.
    """

    def __init__(self, max_iterations=10):
        self.max_iterations = max_iterations
        self.iterations = 0

    def optimize(self, tree):
        previous = repr(tree)

        for iteration in range(1, self.max_iterations + 1):
            tree = CopyPropagation().optimize(tree)
            tree = ConstantPropagation().optimize(tree)
            tree = DeadStoreElimination().optimize(tree)

            current = repr(tree)
            self.iterations = iteration

            if current == previous:
                print()
                print(
                    f"✓ Optimization reached fixed point "
                    f"after {iteration} iteration(s)"
                )
                return tree

            previous = current

        print()
        print(
            f"⚠ Optimization stopped after "
            f"{self.max_iterations} iterations"
        )

        return tree
