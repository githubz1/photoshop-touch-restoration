from __future__ import annotations


class ActionScriptEmitter:

    def __init__(self):
        self.indent = 0

    def line(self, text):
        return "    " * self.indent + text

    def short_name(self, name):
        if name is None:
            return ""

        if "::" in name:
            name = name.split("::", 1)[1]

        return name

    def emit(self, node):

        if node is None:
            return ""

        fn = getattr(
            self,
            f"_emit_{type(node).__name__}",
            None,
        )

        if fn is None:
            return f"<{type(node).__name__}>"

        return fn(node)

    def _emit_Constant(self, node):
        value = node.value

        if value is None:
            return "null"

        if value == "undefined":
            return "undefined"

        if value is True:
            return "true"

        if value is False:
            return "false"

        if isinstance(value, str):
            escaped = (
                value
                .replace("\\", "\\\\")
                .replace('"', '\\"')
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            )
            return f'"{escaped}"'

        return str(value)

    def _emit_Variable(self, node):
        return node.name

    def _emit_LexicalReference(self, node):
        return node.name

    def _emit_ScopeReference(self, node):
        return node.name

    def _emit_ScopeObject(self, node):
        return f"<scope {node.index}>"

    def _emit_Property(self, node):
        receiver = self.emit(node.receiver)
        name = self.short_name(str(node.name))

        return f"{receiver}.{name}"

    def _emit_QualifiedProperty(self, node):
        owner = self.short_name(str(node.owner))
        name = self.short_name(str(node.name))

        return f"{owner}.{name}"

    def _emit_Assignment(self, node):
        return self.line(
            f"{self.emit(node.target)} = "
            f"{self.emit(node.value)};"
        )

    def _emit_Return(self, node):
        operand = getattr(node, "operand", None)

        if operand is None:
            operand = getattr(node, "value", None)

        if operand is None:
            return self.line("return;")

        return self.line(
            f"return {self.emit(operand)};"
        )

    def _emit_Call(self, node):
        callee = getattr(node, "callee", None)

        if callee is None:
            callee = getattr(node, "function", None)

        args = ", ".join(
            self.emit(arg)
            for arg in node.arguments
        )

        return f"{self.emit(callee)}({args})"


    def _emit_Construct(self, node):
        constructor = getattr(node, "constructor", None)
        arguments = getattr(node, "arguments", [])

        args = ", ".join(
            self.emit(arg)
            for arg in arguments
        )

        constructor_text = self.emit(constructor)

        if constructor_text.endswith(".Array"):
            constructor_text = "Array"

        return f"new {constructor_text}({args})"

    def _emit_Unary(self, node):
        operator = node.operator
        operand = self.emit(node.operand)

        if operator == "return ":
            return self.line(
                f"return {operand};"
            )

        return f"{operator}{operand}"


    def _emit_DynamicProperty(self, node):
        return (
            f"{self.emit(node.receiver)}"
            f"[{self.emit(node.key)}]"
        )


    def _emit_BlockNode(self, node):
        lines = []

        for statement in node.statements:

            rendered = self.emit(statement)

            if not rendered:
                continue

            stripped = rendered.strip().rstrip(";")

            # Drop orphan local-variable expressions.
            if (
                stripped.startswith("local")
                and stripped[5:].isdigit()
            ):
                continue

            # Ensure indentation.
            if not rendered.startswith("    "):
                rendered = self.line(rendered)

            # Ensure statement terminator.
            if (
                rendered.strip()
                and not rendered.rstrip().endswith((";", "{", "}"))
            ):
                rendered += ";"

            lines.append(rendered)

        return "\n".join(lines)

    def _emit_IfNode(self, node):
        lines = [
            self.line(
                f"if ({self.emit(node.condition)})"
            ),
            self.line("{"),
        ]

        self.indent += 1

        body = self.emit(node.then_body)

        if body:
            lines.append(body)

        self.indent -= 1
        lines.append(self.line("}"))

        if (
            node.else_body is not None
            and node.else_body.statements
        ):
            lines.append(self.line("else"))
            lines.append(self.line("{"))

            self.indent += 1

            body = self.emit(node.else_body)

            if body:
                lines.append(body)

            self.indent -= 1
            lines.append(self.line("}"))

        return "\n".join(lines)

    def _emit_ForInNode(self, node):
        lines = [
            self.line(
                f"for ({self.emit(node.target)} in "
                f"{self.emit(node.iterable)})"
            ),
            self.line("{"),
        ]

        self.indent += 1

        body = self.emit(node.body)

        if body:
            lines.append(body)

        self.indent -= 1
        lines.append(self.line("}"))

        return "\n".join(lines)


    def _emit_Binary(self, node):
        return (
            f"({self.emit(node.left)} "
            f"{node.operator} "
            f"{self.emit(node.right)})"
        )
