from __future__ import annotations

from phoenix.ast.expressions import Unary, Variable
from phoenix.ast.nodes import (
    BlockNode,
    ForInNode,
    IfNode,
)
from phoenix.recovery.enumeration import EnumerationRecognizer


class RegionBuilder:

    def __init__(self, cfg, states):
        self.cfg = cfg
        self.states = states

    def _append_block_statements(
        self,
        destination,
        block,
        skip_target=None,
    ):
        skipped_target = False

        for statement in self.states[block].statements:

            if (
                not skipped_target
                and skip_target is not None
                and type(statement).__name__ == "Assignment"
                and type(statement.target).__name__ == "Variable"
                and statement.target.name == skip_target
            ):
                # The enumeration target is represented by
                # ForInNode.target, so do not emit its synthetic
                # setlocal assignment inside the loop body.
                skipped_target = True
                continue

            destination.statements.append(statement)

    def build(self):
        if not self.cfg.blocks:
            return BlockNode()

        entry = self.cfg.blocks[0]
        root = BlockNode()

        root.statements.extend(
            self.states[entry].statements
        )

        condition = self.states[entry].condition

        # Linear methods such as getters and setters must not
        # receive a synthetic empty IfNode or duplicate final block.
        if condition is None:
            for block in self.cfg.blocks[1:]:
                self._append_block_statements(
                    root,
                    block,
                )

            return root

        merge = self.cfg.blocks[-1]
        then_body = BlockNode()

        matches = EnumerationRecognizer().recognize(
            self.cfg
        )

        if matches:
            match = matches[0]

            for block in self.cfg.blocks[
                entry.id + 1:match.header.id
            ]:
                self._append_block_statements(
                    then_body,
                    block,
                )

            loop_body = BlockNode()
            target_name = f"local{match.value_local}"

            for block in self.cfg.blocks[
                match.header.id:match.latch.id
            ]:
                self._append_block_statements(
                    loop_body,
                    block,
                    skip_target=(
                        target_name
                        if block is match.header
                        else None
                    ),
                )

            then_body.statements.append(
                ForInNode(
                    target=Variable(target_name),
                    iterable=Variable(
                        f"local{match.object_local}"
                    ),
                    body=loop_body,
                )
            )

            for block in self.cfg.blocks[
                match.latch.id + 1:merge.id
            ]:
                self._append_block_statements(
                    then_body,
                    block,
                )

        else:
            for block in self.cfg.blocks[1:-1]:
                self._append_block_statements(
                    then_body,
                    block,
                )

        root.statements.append(
            IfNode(
                condition=Unary("!", condition),
                then_body=then_body,
                else_body=None,
            )
        )

        root.statements.extend(
            self.states[merge].statements
        )

        return root
