#!/usr/bin/env python3

from pathlib import Path
import sys

PROJECT_ROOT = Path(__file__).resolve().parents[1]

if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from phoenix.abc import parse
from phoenix.analysis.regions import RegionBuilder
from phoenix.analysis.stackflow import StackFlowAnalysis
from phoenix.cfg import CFGBuilder
from phoenix.disassembler import Disassembler
from phoenix.emitter.actionscript import ActionScriptEmitter
from phoenix.optimization.pipeline import OptimizationPipeline


ABC_PATH = Path("output/abc/frame1.abc")
OUTPUT_DIR = Path("output/decompiled/TTApplication")


def decompile_method(abc, method_index):
    body = abc.method_bodies[method_index]

    cfg = CFGBuilder().build(
        Disassembler().decode(body)
    )

    states = StackFlowAnalysis(abc).analyze(cfg)

    tree = RegionBuilder(
        cfg,
        states,
    ).build()

    tree = OptimizationPipeline().optimize(tree)

    emitter = ActionScriptEmitter()
    emitter.indent = 1

    body_text = emitter.emit(tree)

    lines = [
        "// Decompiled by Phoenix",
        "",
        f"function method{method_index}()",
        "{",
    ]

    if body_text:
        lines.append(body_text)

    lines.append("}")

    return "\n".join(lines)


def main():
    if len(sys.argv) < 2:
        raise SystemExit(
            "Usage: ./tools/decompile_methods.py METHOD [METHOD ...]"
        )

    method_indices = [
        int(argument)
        for argument in sys.argv[1:]
    ]

    print(f"Parsing {ABC_PATH} once...")
    abc = parse(ABC_PATH.read_bytes())
    print("✓ Parse complete")

    OUTPUT_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    failures = []

    for position, method_index in enumerate(
        method_indices,
        start=1,
    ):
        print(
            f"[{position}/{len(method_indices)}] "
            f"method {method_index}"
        )

        try:
            source = decompile_method(
                abc,
                method_index,
            )

            destination = (
                OUTPUT_DIR
                / f"method{method_index}.as"
            )

            destination.write_text(
                source + "\n"
            )

        except Exception as exc:
            failures.append(
                (method_index, str(exc))
            )

            print(f"  ✗ {exc}")

    print()
    print(
        f"✓ Decompiled "
        f"{len(method_indices) - len(failures)} "
        f"of {len(method_indices)} methods"
    )

    if failures:
        print()
        print("Failures")
        print("--------")

        for method_index, error in failures:
            print(
                f"method {method_index}: {error}"
            )


if __name__ == "__main__":
    main()
