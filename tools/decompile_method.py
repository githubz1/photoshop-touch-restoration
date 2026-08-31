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
DEFAULT_METHOD = 14903


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
    emitter.indent = 0

    return emitter.emit(tree)


def render_method_source(abc, method_index):
    body_text = decompile_method(
        abc,
        method_index,
    )

    lines = [
        "// Decompiled by Phoenix",
        "",
        f"function method{method_index}()",
        "{",
    ]

    if body_text:
        lines.extend(
            "    " + line if line else ""
            for line in body_text.splitlines()
        )

    lines.append("}")

    return "\n".join(lines)


def main():
    method_index = (
        int(sys.argv[1])
        if len(sys.argv) > 1
        else DEFAULT_METHOD
    )

    abc = parse(
        ABC_PATH.read_bytes()
    )

    generated = render_method_source(
        abc,
        method_index,
    )

    print(generated)

    destination = (
        Path("output/decompiled")
        / f"method{method_index}.as"
    )

    destination.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    destination.write_text(
        generated + "\n"
    )

    print()
    print(f"✓ Wrote {destination}")


if __name__ == "__main__":
    main()
