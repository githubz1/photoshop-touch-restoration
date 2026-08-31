from __future__ import annotations


class ConstantPropagation:

    def optimize(self, tree):
        self.environment = {}
        self._visit(tree)
        return tree

    def _visit(self, node):
        if node is None:
            return

        node_type = type(node).__name__

        if node_type == "BlockNode":
            for statement in node.statements:
                self._visit(statement)
            return

        if node_type == "IfNode":
            incoming = dict(self.environment)

            self._visit(node.then_body)

            self.environment = incoming

            if node.else_body is not None:
                self._visit(node.else_body)

            self.environment = {}
            return

        if node_type == "ForInNode":
            # Propagate the iterable itself when possible.
            if type(node.iterable).__name__ == "Variable":
                replacement = self.environment.get(
                    node.iterable.name
                )

                if replacement is not None:
                    node.iterable = replacement
            else:
                self._rewrite(node.iterable)

            incoming = dict(self.environment)
            self.environment = dict(incoming)

            # The loop target receives a fresh property name on
            # every iteration. Its pre-loop constant is invalid.
            if type(node.target).__name__ == "Variable":
                self.environment.pop(
                    node.target.name,
                    None,
                )

            self._visit(node.body)

            # Conservatively discard constants after the loop.
            self.environment = {}
            return


        if node_type == "Assignment":

            self._rewrite(node.value)

            target = node.target

            if type(target).__name__ == "Variable":

                if type(node.value).__name__ == "Constant":
                    self.environment[target.name] = node.value
                else:
                    self.environment.pop(
                        target.name,
                        None,
                    )

            return

        self._rewrite(node)

    def _rewrite(self, node):
        if node is None:
            return

        if not hasattr(node, "__dict__"):
            return

        for name, value in vars(node).items():

            if (
                type(value).__name__ == "Variable"
                and value.name in self.environment
            ):
                setattr(
                    node,
                    name,
                    self.environment[value.name],
                )

            elif isinstance(value, list):
                for item in value:
                    self._rewrite(item)

            elif hasattr(value, "__dict__"):
                self._rewrite(value)
