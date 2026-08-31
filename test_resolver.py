from pathlib import Path

from phoenix.abc import parse
from phoenix.disassembler import Disassembler
from phoenix.resolver import Resolver

abc = parse(Path("output/abc/frame1.abc").read_bytes())

body = max(
    abc.method_bodies,
    key=lambda b: len(b.code)
)

resolver = Resolver(abc)

for ins in Disassembler().decode(body)[:50]:
    r = resolver.resolve(ins)
    print(
        f"{r.offset:04X}",
        f"{r.mnemonic:<20}",
        *r.operands,
    )
