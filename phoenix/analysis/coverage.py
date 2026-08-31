from __future__ import annotations

from collections import Counter


class CoverageAnalysis:

    def opcode_coverage(self, instructions, simulator):

        total = Counter()
        supported = Counter()

        for ins in instructions:

            total[ins.mnemonic] += 1

            before = (
                simulator.depth(),
                len(simulator.statements),
                tuple(type(x).__name__ for x in simulator.stack),
                repr(simulator.stack[-1]) if simulator.stack else None,
            )

            try:
                simulator.execute(ins)
            except Exception:
                continue

            after = (
                simulator.depth(),
                len(simulator.statements),
                tuple(type(x).__name__ for x in simulator.stack),
                repr(simulator.stack[-1]) if simulator.stack else None,
            )

            if after != before:
                supported[ins.mnemonic] += 1

        return total, supported
