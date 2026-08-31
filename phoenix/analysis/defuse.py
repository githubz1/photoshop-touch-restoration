from __future__ import annotations

from dataclasses import dataclass

from phoenix.analysis.reaching import (
    PhiDefinition,
    ReachingDefinitions,
)


@dataclass(eq=False)
class Definition:
    id: int
    variable: str
    node: object


@dataclass(eq=False)
class Use:
    variable: str
    node: object
    reaching_definition: object = None


class DefUseAnalyzer:

    def __init__(self):
        self.definitions: list[Definition] = []
        self.uses: list[Use] = []
        self.definition_uses: dict[Definition, list[Use]] = {}
        self._next_definition_id = 0
        self.reaching = ReachingDefinitions()

    def analyze(self, tree):
        self.definitions.clear()
        self.uses.clear()
        self.definition_uses.clear()
        self._next_definition_id = 0

        self._analyze_node(tree, {})
        return self

    def uses_of(self, definition):
        return list(
            self.definition_uses.get(definition, [])
        )

    def unused_definitions(self):
        return [
            definition
            for definition in self.definitions
            if not self.definition_uses.get(definition)
        ]

    def _new_definition(self, variable, node):
        definition = Definition(
            id=self._next_definition_id,
            variable=variable,
            node=node,
        )

        self._next_definition_id += 1
        self.definitions.append(definition)
        self.definition_uses[definition] = []

        return definition

    def _link_use(self, reaching, use):
        if reaching is None:
            return

        if isinstance(reaching, Definition):
            self.definition_uses[reaching].append(use)
            return

        if isinstance(reaching, PhiDefinition):
            seen = set()

            for incoming in reaching.incoming:
                if incoming is None:
                    continue

                identity = id(incoming)

                if identity in seen:
                    continue

                seen.add(identity)
                self._link_use(incoming, use)

    def _record_use(self, variable, node, environment):
        reaching = environment.get(variable)

        use = Use(
            variable=variable,
            node=node,
            reaching_definition=reaching,
        )

        self.uses.append(use)
        self._link_use(reaching, use)

        return use

    def _analyze_node(self, node, environment):
        if node is None:
            return dict(environment)

        node_type = type(node).__name__

        if node_type == "BlockNode":
            current = dict(environment)

            for statement in node.statements:
                current = self._analyze_node(
                    statement,
                    current,
                )

            return current

        if node_type == "IfNode":
            self._analyze_expression(
                node.condition,
                environment,
            )

            then_environment = self._analyze_node(
                node.then_body,
                dict(environment),
            )

            if node.else_body is None:
                else_environment = dict(environment)
            else:
                else_environment = self._analyze_node(
                    node.else_body,
                    dict(environment),
                )

            return self.reaching.merge(
                then_environment,
                else_environment,
            )

        if node_type == "ForInNode":
            self._analyze_expression(
                node.iterable,
                environment,
            )

            current = dict(environment)

            if type(node.target).__name__ == "Variable":
                definition = self._new_definition(
                    node.target.name,
                    node,
                )
                current[node.target.name] = definition
            else:
                self._analyze_expression(
                    node.target,
                    current,
                )

            body_environment = self._analyze_node(
                node.body,
                current,
            )

            return self.reaching.merge(
                environment,
                body_environment,
            )


        if node_type == "Assignment":
            current = dict(environment)

            self._analyze_expression(
                node.value,
                current,
            )

            target = node.target

            if type(target).__name__ == "Variable":
                definition = self._new_definition(
                    target.name,
                    node,
                )

                current[target.name] = definition
            else:
                self._analyze_expression(
                    target,
                    current,
                )

            return current

        self._analyze_expression(
            node,
            environment,
        )

        return dict(environment)

    def _analyze_expression(self, node, environment):
        if node is None:
            return

        node_type = type(node).__name__

        if node_type == "Variable":
            self._record_use(
                node.name,
                node,
                environment,
            )
            return

        if node_type in {
            "BlockNode",
            "IfNode",
            "Assignment",
        }:
            self._analyze_node(
                node,
                environment,
            )
            return

        if not hasattr(node, "__dict__"):
            return

        for value in vars(node).values():
            if isinstance(value, (list, tuple)):
                for item in value:
                    self._analyze_expression(
                        item,
                        environment,
                    )

            elif hasattr(value, "__dict__"):
                self._analyze_expression(
                    value,
                    environment,
                )
