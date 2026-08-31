"""
Constant-pool resolver for AVM2.
"""

from dataclasses import dataclass
from phoenix.formatter import Formatter


@dataclass
class ResolvedInstruction:
    offset: int
    opcode: int
    mnemonic: str
    operands: list


class Resolver:

    def __init__(self, abc):
        self.abc = abc
        self.fmt = Formatter(abc)

    def resolve(self, instruction):

        operands = list(instruction.operands)

        # Resolve string constants.
        if (
            instruction.mnemonic == "pushstring"
            and operands
        ):
            index = operands[0]

            if 0 <= index < len(self.abc.strings):
                operands[0] = repr(
                    self.abc.strings[index]
                )

        # Resolve multiname references.
        elif (
            instruction.mnemonic in {
                "coerce",
                "astype",
                "getlex",
                "findproperty",
                "findpropstrict",
                "getproperty",
                "setproperty",
                "initproperty",
                "deleteproperty",
                "callproperty",
                "callproplex",
                "callpropvoid",
                "constructprop",
            }
            and operands
        ):
            index = operands[0]

            if 0 <= index < len(self.abc.multinames):
                operands[0] = self.fmt.multiname(self.abc.multinames[index])

        return ResolvedInstruction(
            offset=instruction.offset,
            opcode=instruction.opcode,
            mnemonic=instruction.mnemonic,
            operands=operands,
        )
