from __future__ import annotations


class ASTVisitor:
    """
    Generic AST visitor.

    Every optimization pass should inherit from this class.
    """

    def visit(self, node):

        if node is None:
            return None

        method = getattr(
            self,
            f"visit_{type(node).__name__}",
            self.generic_visit,
        )

        return method(node)

    def generic_visit(self, node):

        for value in vars(node).values():

            if isinstance(value, list):

                for item in value:
                    if hasattr(item, "__dict__"):
                        self.visit(item)

            elif hasattr(value, "__dict__"):
                self.visit(value)

        return node
