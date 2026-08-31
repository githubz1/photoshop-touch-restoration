"""
AVM2 bytecode disassembler.
"""

from phoenix.bytecode import BytecodeReader
from phoenix.opcodes import OPCODES
from phoenix.abc_model import Instruction


class Disassembler:

    def decode(self, body):

        if body.instructions:

            return body.instructions


        body.instructions = self._decode_bytes(body.code)

        return body.instructions


    def _decode_bytes(self, code: bytes):
        reader = BytecodeReader(code)
        instructions = []

        while not reader.eof:

            offset = reader.tell()

            opcode = reader.read_u8()

            definition = OPCODES.get(opcode)

            if definition is None:
                raise ValueError(
                    f"Unknown AVM2 opcode 0x{opcode:02X} "
                    f"at bytecode offset 0x{offset:04X}"
                )

            mnemonic, operands = definition

            values = []

            # Special-case AVM2 lookupswitch.
            if mnemonic == "lookupswitch":

                default_offset = reader.read_s24()
                case_count = reader.read_u30()

                case_offsets = [
                    reader.read_s24()
                    for _ in range(case_count + 1)
                ]

                values = [
                    default_offset,
                    case_count,
                    *case_offsets,
                ]

            else:

                for operand in operands:

                    if operand == "u30":
                        values.append(reader.read_u30())

                    elif operand == "s24":
                        values.append(reader.read_s24())

                    elif operand == "byte":
                        values.append(reader.read_u8())

                    elif operand == "u8":
                        values.append(reader.read_u8())

                    else:
                        raise ValueError(
                            f"Unknown operand type: {operand}"
                        )

            instructions.append(
                Instruction(
                    offset=offset,
                    opcode=opcode,
                    mnemonic=mnemonic,
                    operands=values,
                )
            )

        return instructions
