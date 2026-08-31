from __future__ import annotations

import argparse
from collections import Counter
from pathlib import Path

from phoenix.abc import parse as parse_abc
from phoenix.apk import APK
from phoenix.swf import parse as parse_swf
from phoenix.tags import parse_doabc, parse_tags, tag_name


def inspect(apk_path: str) -> int:
    path = Path(apk_path).expanduser()

    if not path.exists():
        print(f"File not found: {path}")
        return 1

    with APK(path) as apk:
        print(f"Opened APK : {apk.path}")

        entry = apk.find("TTPixel.swf")

        if entry is None:
            print("TTPixel.swf not found.")
            return 1

        print(f"SWF Entry  : {entry}")

        swf = parse_swf(apk.read(entry))
        tags = parse_tags(swf.data)

        print()
        print("SWF")
        print("---")
        print(f"Signature : {swf.signature}")
        print(f"Version   : {swf.version}")
        print(f"Length    : {swf.file_length:,} bytes")
        print(f"Tags      : {len(tags):,}")

        print()
        print("First 20 tags")
        print("-------------")

        for tag in tags[:20]:
            print(
                f"{tag.offset:8d}  "
                f"code={tag.code:3d}  "
                f"{tag_name(tag.code):32s}  "
                f"length={tag.length:8d}"
            )

        counts = Counter(tag.code for tag in tags)

        print()
        print("Tag summary")
        print("-----------")

        for code, count in sorted(
            counts.items(),
            key=lambda item: (-item[1], item[0]),
        ):
            print(
                f"{code:3d}  "
                f"{tag_name(code):32s}  "
                f"{count:6d}"
            )

        print()
        print(f"DoABC tags : {counts.get(82, 0):,}")

    return 0


def extract_abc(apk_path: str, output_dir: str) -> int:
    path = Path(apk_path).expanduser()
    destination = Path(output_dir).expanduser()

    if not path.exists():
        print(f"File not found: {path}")
        return 1

    destination.mkdir(parents=True, exist_ok=True)

    with APK(path) as apk:
        entry = apk.find("TTPixel.swf")

        if entry is None:
            print("TTPixel.swf not found.")
            return 1

        swf = parse_swf(apk.read(entry))
        tags = parse_tags(swf.data)
        doabc_tags = [tag for tag in tags if tag.code == 82]

        if not doabc_tags:
            print("No DoABC tags found.")
            return 1

        print(f"DoABC tags : {len(doabc_tags):,}")

        for index, tag in enumerate(doabc_tags, start=1):
            block = parse_doabc(tag)
            abc = parse_abc(block.abc)

            safe_name = block.name.strip() or f"application_{index:03d}"
            safe_name = "".join(
                character
                if character.isalnum() or character in ("-", "_", ".")
                else "_"
                for character in safe_name
            )

            output_path = destination / f"{safe_name}.abc"
            output_path.write_bytes(block.abc)

            print()
            print(f"Wrote      : {output_path}")
            print(f"Flags      : 0x{block.flags:08X}")
            print(f"Name       : {block.name or '<empty>'}")
            print(f"ABC size   : {len(block.abc):,} bytes")

            print()
            print("ABC")
            print("---")
            print(f"Minor version : {abc.minor}")
            print(f"Major version : {abc.major}")
            print(f"Integers      : {len(abc.ints) - 1:,}")
            print(f"UIntegers     : {len(abc.uints) - 1:,}")
            print(f"Doubles       : {len(abc.doubles) - 1:,}")
            print(f"Strings       : {len(abc.strings) - 1:,}")
            print(f"Namespaces    : {abc.namespace_count:,}")
            print(f"NS Sets       : {abc.namespace_set_count:,}")
            print(f"Multinames    : {abc.multiname_count:,}")
            print(f"Methods       : {abc.method_count:,}")

            print()
            print("First strings")
            print("-------------")

            for string_index, value in enumerate(
                abc.strings[1:21],
                start=1,
            ):
                print(f"{string_index:4d}  {value}")

    return 0


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="phoenix",
        description="Photoshop Touch reverse engineering toolkit",
    )

    subparsers = parser.add_subparsers(dest="command")

    inspect_parser = subparsers.add_parser(
        "inspect",
        help="Inspect the TTPixel.swf embedded in an APK.",
    )
    inspect_parser.add_argument("apk")

    extract_parser = subparsers.add_parser(
        "extract-abc",
        help="Extract and inspect DoABC payloads from TTPixel.swf.",
    )
    extract_parser.add_argument("apk")
    extract_parser.add_argument(
        "--output",
        default="output/abc",
        help="Directory for extracted .abc files.",
    )

    args = parser.parse_args()

    if args.command == "inspect":
        raise SystemExit(inspect(args.apk))

    if args.command == "extract-abc":
        raise SystemExit(extract_abc(args.apk, args.output))

    parser.print_help()


if __name__ == "__main__":
    main()
