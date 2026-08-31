from __future__ import annotations

from phoenix.ast.nodes import (
    BlockNode,
    WhileNode,
    IfNode,
    InstructionNode,
)


class ASTBuilder:

    def build(self, structured_regions):

        root = BlockNode()

        for region in sorted(
            structured_regions,
            key=lambda r: r.header.id,
        ):

            if region.kind == "loop":

                node = WhileNode()

            elif region.kind == "if":

                node = IfNode()

            else:

                node = InstructionNode(region)

            root.statements.append(node)

        return root
