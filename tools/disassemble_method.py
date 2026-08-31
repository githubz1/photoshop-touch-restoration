#!/usr/bin/env python3

from pathlib import Path
import sys

PROJECT_ROOT = Path(__file__).resolve().parents[1]

if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from phoenix.abc import parse
from phoenix.disassembler import Disassembler


ABC_PATH = Path("output/abc/frame1.abc")


def main():
    if len(sys.argv) != 2:
        raise SystemExit(
            "Usage: ./tools/disassemble_method.py METHOD_BODY_INDEX"
        )

    method_index = int(sys.argv[1])

    abc = parse(
        ABC_PATH.read_bytes()
    )

    if not 0 <= method_index < len(abc.method_bodies):
        raise SystemExit(
            f"Method body index out of range: {method_index}"
        )

    body = abc.method_bodies[method_index]

    print(f"Method body index: {method_index}")
    print(f"Method index     : {body.method_index}")
    print(f"Max stack        : {body.max_stack}")
    print(f"Local count      : {body.local_count}")
    print(f"Code length      : {len(body.code)}")
    print()
    print("Instructions")
    print("------------")

    instructions = Disassembler().decode(body)

    for instruction in instructions:
        operands = ", ".join(
            str(value)
            for value in instruction.operands
        )

        print(
            f"{instruction.offset:06X}  "
            f"{instruction.mnemonic:<24} "
            f"{operands}"
        )


if __name__ == "__main__":
    main()
