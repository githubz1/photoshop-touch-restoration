from __future__ import annotations

from dataclasses import dataclass, field


BRANCH_OPCODES = {
    "jump",
    "iftrue",
    "iffalse",
    "ifeq",
    "ifne",
    "iflt",
    "ifle",
    "ifgt",
    "ifge",
    "ifstricteq",
    "ifstrictne",
    "ifnlt",
    "ifnle",
    "ifngt",
    "ifnge",
    "lookupswitch",
}

TERMINAL_OPCODES = {
    "returnvoid",
    "returnvalue",
    "throw",
}


@dataclass(eq=False)
class BasicBlock:
    id: int
    start_offset: int
    instructions: list = field(default_factory=list)
    successors: list["BasicBlock"] = field(default_factory=list)
    predecessors: list["BasicBlock"] = field(default_factory=list)


@dataclass
class ControlFlowGraph:
    blocks: list[BasicBlock] = field(default_factory=list)

    def block_at(self, offset):
        for block in self.blocks:
            if block.start_offset == offset:
                return block
        return None

    def to_dot(self):
        lines = [
            "digraph CFG {",
            '  node [shape=box, fontname="Courier"];',
        ]

        for block in self.blocks:
            label = "\\l".join(
                f"{instr.offset:04X}: {instr.mnemonic}"
                for instr in block.instructions
            ) + "\\l"

            lines.append(
                f'  B{block.id} [label="B{block.id}\\l{label}"];'
            )

        for block in self.blocks:
            for succ in block.successors:
                lines.append(
                    f"  B{block.id} -> B{succ.id};"
                )

        lines.append("}")

        return "\n".join(lines)


def is_branch(instr):
    return instr.mnemonic in BRANCH_OPCODES


def is_terminal(instr):
    return instr.mnemonic in TERMINAL_OPCODES


def find_leaders(instructions):
    if not instructions:
        return []

    leaders = {instructions[0].offset}
    valid_offsets = {i.offset for i in instructions}

    for index, instr in enumerate(instructions):

        next_offset = (
            instructions[index + 1].offset
            if index + 1 < len(instructions)
            else None
        )

        if instr.mnemonic == "lookupswitch":

            for operand in instr.operands:
                if isinstance(operand, int):
                    target = instr.offset + operand
                    if target in valid_offsets:
                        leaders.add(target)

            if next_offset is not None:
                leaders.add(next_offset)

        elif is_branch(instr):

            if instr.operands and isinstance(instr.operands[0], int):

                target = next_offset + instr.operands[0]

                if target in valid_offsets:
                    leaders.add(target)

            if instr.mnemonic != "jump" and next_offset is not None:
                leaders.add(next_offset)

        elif is_terminal(instr):

            if next_offset is not None:
                leaders.add(next_offset)

    return sorted(leaders)




    def to_dot(self):
        lines = [
            "digraph CFG {",
            "  node [shape=box,fontname=Courier];",
        ]

        for block in self.blocks:
            label = "\n".join(
                f"{instr.offset:04X}: {instr.mnemonic}"
                for instr in block.instructions
            )

            lines.append(
                f'  B{block.id} [label="{label}"];'
            )

        for block in self.blocks:
            for succ in block.successors:
                lines.append(
                    f"  B{block.id} -> B{succ.id};"
                )

        lines.append("}")

        return "\n".join(lines)


class CFGBuilder:

    def build(self, instructions):

        cfg = ControlFlowGraph()

        leaders = set(find_leaders(instructions))

        current = None

        for instr in instructions:

            if current is None or instr.offset in leaders:
                current = BasicBlock(
                    id=len(cfg.blocks),
                    start_offset=instr.offset,
                )
                cfg.blocks.append(current)

            current.instructions.append(instr)

        self._connect(cfg)

        return cfg


    def _connect(self, cfg):

        lookup = {
            block.start_offset: block
            for block in cfg.blocks
        }

        offsets = [
            block.start_offset
            for block in cfg.blocks
        ]

        for index, block in enumerate(cfg.blocks):

            last = block.instructions[-1]

            next_offset = (
                offsets[index + 1]
                if index + 1 < len(offsets)
                else None
            )

            def add_edge(offset):

                target = lookup.get(offset)

                if target is None:
                    return

                if target not in block.successors:
                    block.successors.append(target)

                if block not in target.predecessors:
                    target.predecessors.append(block)

            if last.mnemonic in ("returnvoid", "returnvalue", "throw"):
                continue

            if last.mnemonic in ("jump",):

                if last.operands:
                    target = (
                        last.offset
                        + 4
                        + last.operands[0]
                    )

                    add_edge(target)

                continue

            if last.mnemonic.startswith("if"):

                if next_offset is not None:
                    add_edge(next_offset)

                if last.operands:
                    target = (
                        last.offset
                        + 4
                        + last.operands[0]
                    )

                    add_edge(target)

                continue

            if next_offset is not None:
                add_edge(next_offset)
