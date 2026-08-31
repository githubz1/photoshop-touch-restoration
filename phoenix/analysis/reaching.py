from __future__ import annotations

from dataclasses import dataclass


@dataclass(eq=False)
class PhiDefinition:
    variable: str
    incoming: tuple


class ReachingDefinitions:

    def merge(self, left, right):
        merged = {}

        for variable in set(left) | set(right):
            lhs = left.get(variable)
            rhs = right.get(variable)

            if lhs is rhs:
                merged[variable] = lhs
            else:
                merged[variable] = PhiDefinition(
                    variable=variable,
                    incoming=(lhs, rhs),
                )

        return merged
