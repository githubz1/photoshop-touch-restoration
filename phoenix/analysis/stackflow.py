from __future__ import annotations

from collections import deque
from dataclasses import dataclass, field

from phoenix.ast.stack import StackSimulator


@dataclass
class BlockState:
    entry_stack: list = field(default_factory=list)
    exit_stack: list = field(default_factory=list)
    statements: list = field(default_factory=list)
    condition: object = None
    failures: list = field(default_factory=list)


class StackFlowAnalysis:

    def __init__(self, abc):
        self.abc = abc

    def _merge_stacks(self, existing, incoming):
        if existing is None:
            return list(incoming)

        if len(existing) != len(incoming):
            # Keep only the common prefix until proper phi-node support exists.
            limit = min(len(existing), len(incoming))
        else:
            limit = len(existing)

        merged = []

        for index in range(limit):
            left = existing[index]
            right = incoming[index]

            if left == right:
                merged.append(left)
            else:
                merged.append(left)

        return merged

    def analyze(self, cfg):

        if not cfg.blocks:
            return {}

        states = {
            block: BlockState()
            for block in cfg.blocks
        }

        incoming = {
            block: None
            for block in cfg.blocks
        }

        entry = cfg.blocks[0]
        incoming[entry] = []

        queue = deque([entry])
        queued = {entry}

        while queue:
            block = queue.popleft()
            queued.discard(block)

            simulator = StackSimulator(self.abc)
            simulator.stack = list(incoming[block] or [])

            state = states[block]
            state.entry_stack = list(simulator.stack)
            state.failures = []

            for instruction in block.instructions:
                try:
                    simulator.execute(instruction)
                except Exception as exc:
                    state.failures.append(
                        (
                            instruction.offset,
                            instruction.mnemonic,
                            str(exc),
                        )
                    )

            state.exit_stack = list(simulator.stack)
            state.statements = list(simulator.statements)
            state.condition = simulator.branch_condition

            for successor in block.successors:
                merged = self._merge_stacks(
                    incoming[successor],
                    state.exit_stack,
                )

                if merged != incoming[successor]:
                    incoming[successor] = merged

                    if successor not in queued:
                        queue.append(successor)
                        queued.add(successor)

        return states
