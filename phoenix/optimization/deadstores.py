from __future__ import annotations

from phoenix.analysis.defuse import DefUseAnalyzer

class DeadStoreElimination:


    def _trim_after_return(self, statements):
        """
        Remove unreachable statements after the first return.
        """
        result = []

        for stmt in statements:
            result.append(stmt)

            if type(stmt).__name__ == "ReturnStatement":
                break

        return result

    """
    Conservatively removes unused local-variable definitions.

    A definition is removed only when:
    - DefUseAnalyzer reports no uses, including phi uses;
    - the assignment target is a local Variable;
    - evaluating the right-hand side has no observable side effects.
    """

    PURE_LEAF_TYPES = {
        "Constant",
        "Variable",
        "LexicalReference",
        "ScopeReference",
        "ScopeObject",
        "GlobalScope",
    }

    PURE_COMPOSITE_TYPES = {
        "Unary",
        "Binary",
    }

    def __init__(self):
        self.removed = []

    def optimize(self, tree):
        analysis = DefUseAnalyzer().analyze(tree)

        self._unused_node_ids = {
            id(definition.node)
            for definition in analysis.unused_definitions()
        }

        self.removed = []
        self._optimize_node(tree)

        if self.removed:
            print()
            print("Removed dead stores")
            print("-------------------")

            for assignment in self.removed:
                target = getattr(assignment, "target", None)
                name = getattr(target, "name", "<unknown>")
                print(name)

        return tree

    def _is_pure(self, node):
        if node is None:
            return True

        node_type = type(node).__name__

        if node_type in self.PURE_LEAF_TYPES:
            return True

        if node_type == "Unary":
            return self._is_pure(node.operand)

        if node_type == "Binary":
            return (
                self._is_pure(node.left)
                and self._is_pure(node.right)
            )

        # Property reads may invoke getters. Calls, constructors,
        # allocations and dynamic accesses may also have effects.
        return False

    def _can_remove(self, statement):
        if type(statement).__name__ != "Assignment":
            return False

        if id(statement) not in self._unused_node_ids:
            return False

        target = getattr(statement, "target", None)

        if type(target).__name__ != "Variable":
            return False

        return self._is_pure(statement.value)

    def _optimize_node(self, node):
        if node is None:
            return

        node_type = type(node).__name__

        if node_type == "BlockNode":
            retained = []

            for statement in node.statements:
                if self._can_remove(statement):
                    self.removed.append(statement)
                    continue

                self._optimize_node(statement)
                retained.append(statement)

            node.statements = retained
            return

        if node_type == "IfNode":
            self._optimize_node(node.then_body)
            self._optimize_node(node.else_body)
            return

        if node_type == "ForInNode":
            self._optimize_node(node.body)
            return


        if node_type == "WhileNode":
            self._optimize_node(node.body)
            return
