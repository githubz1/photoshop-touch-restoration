from __future__ import annotations

from dataclasses import dataclass


@dataclass
class EnumerationMatch:
    header: object
    latch: object
    object_local: int
    iterator_local: int
    value_local: int | None
    kind: str | None


class EnumerationRecognizer:

    def _local_index_from_setlocal(self, instruction):
        mnemonic = instruction.mnemonic

        shortcuts = {
            "setlocal0": 0,
            "setlocal1": 1,
            "setlocal2": 2,
            "setlocal3": 3,
        }

        if mnemonic in shortcuts:
            return shortcuts[mnemonic]

        if mnemonic == "setlocal" and instruction.operands:
            return instruction.operands[0]

        return None

    def recognize(self, cfg):
        matches = []

        for latch in cfg.blocks:
            hasnext = None

            for instruction in latch.instructions:
                if instruction.mnemonic == "hasnext2":
                    hasnext = instruction
                    break

            if hasnext is None:
                continue

            if len(hasnext.operands) < 2:
                continue

            object_local = hasnext.operands[0]
            iterator_local = hasnext.operands[1]

            header = None

            for successor in latch.successors:
                if successor.id <= latch.id:
                    header = successor
                    break

            if header is None:
                continue

            kind = None
            value_local = None

            instructions = header.instructions

            for index, instruction in enumerate(instructions):
                if instruction.mnemonic not in {
                    "nextname",
                    "nextvalue",
                }:
                    continue

                kind = instruction.mnemonic

                for following in instructions[index + 1:]:
                    local = self._local_index_from_setlocal(
                        following
                    )

                    if local is not None:
                        value_local = local
                        break

                break

            if kind is None:
                continue

            matches.append(
                EnumerationMatch(
                    header=header,
                    latch=latch,
                    object_local=object_local,
                    iterator_local=iterator_local,
                    value_local=value_local,
                    kind=kind,
                )
            )

        return matches
