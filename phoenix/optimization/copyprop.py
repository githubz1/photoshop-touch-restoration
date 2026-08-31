from __future__ import annotations


class CopyPropagation:

    def optimize(self, tree):
        self.environment = {}
        self._visit(tree)
        return tree

    def _assigned_variables(self, node):
        assigned = set()

        if node is None:
            return assigned

        node_type = type(node).__name__

        if node_type == "Assignment":
            target = node.target

            if type(target).__name__ == "Variable":
                assigned.add(target.name)

            return assigned

        if node_type == "BlockNode":
            for statement in node.statements:
                assigned.update(
                    self._assigned_variables(statement)
                )

            return assigned

        if node_type == "IfNode":
            assigned.update(
                self._assigned_variables(node.then_body)
            )
            assigned.update(
                self._assigned_variables(node.else_body)
            )

            return assigned

        if node_type == "ForInNode":
            if type(node.target).__name__ == "Variable":
                assigned.add(node.target.name)

            assigned.update(
                self._assigned_variables(node.body)
            )

            return assigned


        if node_type == "WhileNode":
            assigned.update(
                self._assigned_variables(node.body)
            )

        return assigned

    def _visit(self, node):
        if node is None:
            return

        node_type = type(node).__name__

        if node_type == "BlockNode":
            for statement in node.statements:
                self._visit(statement)

            return

        if node_type == "IfNode":
            self._rewrite(node.condition)

            incoming = dict(self.environment)
            assigned = self._assigned_variables(node)

            self.environment = dict(incoming)
            self._visit(node.then_body)

            self.environment = dict(incoming)

            if node.else_body is not None:
                self._visit(node.else_body)

            # Any variable written in either branch is ambiguous
            # after the merge until proper phi-aware propagation exists.
            self.environment = {
                name: value
                for name, value in incoming.items()
                if name not in assigned
            }

            return

        if node_type == "ForInNode":
            # The iterable is a direct child expression, so replace it
            # explicitly when it is a propagated variable.
            if type(node.iterable).__name__ == "Variable":
                replacement = self.environment.get(
                    node.iterable.name
                )

                if replacement is not None:
                    node.iterable = replacement
            else:
                self._rewrite(node.iterable)

            incoming = dict(self.environment)
            assigned = self._assigned_variables(node)

            self.environment = dict(incoming)

            # The enumeration target receives a fresh value on every
            # iteration. Never propagate its pre-loop value into the body.
            if type(node.target).__name__ == "Variable":
                self.environment.pop(
                    node.target.name,
                    None,
                )

            self._visit(node.body)

            self.environment = {
                name: value
                for name, value in incoming.items()
                if name not in assigned
            }
            return

        if node_type == "WhileNode":
            self._rewrite(node.condition)

            incoming = dict(self.environment)
            assigned = self._assigned_variables(node.body)

            self.environment = dict(incoming)
            self._visit(node.body)

            # Loop bodies may execute zero or many times.
            self.environment = {
                name: value
                for name, value in incoming.items()
                if name not in assigned
            }

            return

        if node_type == "Assignment":
            self._rewrite(node.value)

            target = node.target

            if type(target).__name__ == "Variable":
                value = node.value

                if type(value).__name__ in {
                    "Variable",
                    "Constant",
                }:
                    self.environment[target.name] = value
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
            if type(value).__name__ == "Variable":
                replacement = self.environment.get(
                    value.name
                )

                if replacement is not None:
                    setattr(
                        node,
                        name,
                        replacement,
                    )

            elif isinstance(value, list):
                for item in value:
                    self._rewrite(item)

            elif hasattr(value, "__dict__"):
                self._rewrite(value)
