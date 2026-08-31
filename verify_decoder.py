from pathlib import Path

from phoenix.abc import parse
from phoenix.disassembler import Disassembler

abc = parse(Path("output/abc/frame1.abc").read_bytes())

decoder = Disassembler()

ok = 0
failed = 0

for i, body in enumerate(abc.method_bodies):
    try:
        decoder.decode(body.code)
        ok += 1
    except Exception as exc:
        failed += 1
        print(f"Method {i}: {exc}")

print()
print("====================================")
print(f"Decoded : {ok}")
print(f"Failed  : {failed}")
print(f"Total   : {len(abc.method_bodies)}")
