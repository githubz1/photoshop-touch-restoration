#!/usr/bin/env python3

from pathlib import Path
import re
import sys

PROJECT_ROOT = Path(__file__).resolve().parents[1]

if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from phoenix.abc import parse
from tools.decompile_method import decompile_method


ABC_PATH = Path("output/abc/frame1.abc")
OUTPUT_ROOT = Path("output/decompiled/classes")


def pool_value(pool, index):
    if index == 0:
        return ""

    if 0 <= index < len(pool):
        return pool[index]

    return f"<invalid:{index}>"


def namespace_name(abc, index):
    if index == 0:
        return ""

    namespace = abc.namespaces[index]
    return str(
        pool_value(
            abc.strings,
            getattr(namespace, "name_index", 0),
        )
    )


def resolve_multiname(abc, index):
    if index == 0:
        return "*"

    multiname = abc.multinames[index]
    data = multiname.data

    name = str(
        pool_value(
            abc.strings,
            data.get("name_index", 0),
        )
    )

    namespace = namespace_name(
        abc,
        data.get("namespace_index", 0),
    )

    return f"{namespace}.{name}" if namespace else name


def short_name(name):
    name = name.rsplit("::", 1)[-1]
    name = name.rsplit(".", 1)[-1]
    name = name.rsplit(":", 1)[-1]
    return name


def identifier(name, fallback):
    cleaned = re.sub(
        r"[^A-Za-z0-9_$]",
        "_",
        name,
    )

    if not cleaned:
        return fallback

    if cleaned[0].isdigit():
        cleaned = "_" + cleaned

    return cleaned


def indent_text(text, levels):
    prefix = "    " * levels

    return "\n".join(
        prefix + line if line else ""
        for line in text.splitlines()
    )


def recover_body(abc, method_index):
    try:
        return decompile_method(
            abc,
            method_index,
        )
    except Exception as exc:
        return (
            f"// Phoenix could not decompile "
            f"method {method_index}: {exc}"
        )


def render_function(
    abc,
    method_index,
    declaration,
    name,
    return_type="*",
    parameter_list="",
):
    body = recover_body(
        abc,
        method_index,
    )

    # Setter bytecode uses local1 for its sole parameter.
    # Rewrite it to match the emitted parameter name.
    if "function set" in declaration:
        import re

        body = re.sub(
            r"\blocal1\b",
            "value",
            body,
        )

    # Phoenix currently represents returnvoid as a string literal
    # in some recovered bodies.
    body = body.replace(
        '"return";',
        "return;",
    )

    lines = [
        f"{declaration} {name}({parameter_list}):{return_type}",
        "{",
    ]

    if body:
        lines.append(
            indent_text(body, 1)
        )

    lines.append("}")

    return "\n".join(lines)


def render_constructor(
    abc,
    method_index,
    class_name,
):
    body = recover_body(
        abc,
        method_index,
    )

    lines = [
        f"public function {class_name}()",
        "{",
    ]

    if body:
        lines.append(
            indent_text(body, 1)
        )

    lines.append("}")

    return "\n".join(lines)


def main():
    if len(sys.argv) != 2:
        raise SystemExit(
            "Usage: ./tools/decompile_class.py "
            "<fully-qualified-class>"
        )

    target = sys.argv[1]

    print(f"Parsing {ABC_PATH} once...")
    abc = parse(
        ABC_PATH.read_bytes()
    )
    print("✓ Parse complete")

    class_index = None

    for index, instance in enumerate(
        abc.instances
    ):
        name = resolve_multiname(
            abc,
            instance.name_index,
        )

        if name == target:
            class_index = index
            break

    if class_index is None:
        raise SystemExit(
            f"Class not found: {target}"
        )

    instance = abc.instances[class_index]
    class_info = abc.classes[class_index]

    if "." in target:
        package_name, raw_class_name = (
            target.rsplit(".", 1)
        )
    else:
        package_name = ""
        raw_class_name = target

    class_name = identifier(
        raw_class_name,
        f"Class{class_index}",
    )

    superclass = short_name(
        resolve_multiname(
            abc,
            instance.super_name_index,
        )
    )

    superclass = identifier(
        superclass,
        "Object",
    )

    class_lines = [
        f"public class {class_name} "
        f"extends {superclass}",
        "{",
        indent_text(
            render_constructor(
                abc,
                instance.iinit,
                class_name,
            ),
            1,
        ),
        "",
        indent_text(
            render_function(
                abc,
                class_info.cinit,
                "private static function",
                "__static_init__",
                "void",
            ),
            1,
        ),
    ]

    seen = {}

    for trait in instance.traits:
        if trait.kind not in {1, 2, 3}:
            continue

        raw_name = short_name(
            resolve_multiname(
                abc,
                trait.name_index,
            )
        )

        method_name = identifier(
            raw_name,
            f"method{trait.method_index}",
        )

        duplicate = seen.get(
            method_name,
            0,
        )
        seen[method_name] = duplicate + 1

        if duplicate and trait.kind == 1:
            method_name = (
                f"{method_name}_"
                f"{trait.method_index}"
            )

        if trait.kind == 2:
            declaration = (
                "public function get"
            )
        elif trait.kind == 3:
            declaration = (
                "public function set"
            )
        else:
            declaration = (
                "public function"
            )

        class_lines.extend([
            "",
            indent_text(
                render_function(
                    abc,
                    trait.method_index,
                    declaration,
                    method_name,
                    "void" if trait.kind == 3 else "*",
                    "value:*" if trait.kind == 3 else "",
                ),
                1,
            ),
        ])

    for trait in class_info.traits:
        if trait.kind not in {1, 2, 3}:
            continue

        raw_name = short_name(
            resolve_multiname(
                abc,
                trait.name_index,
            )
        )

        method_name = identifier(
            raw_name,
            f"method{trait.method_index}",
        )

        if trait.kind == 2:
            declaration = (
                "public static function get"
            )
        elif trait.kind == 3:
            declaration = (
                "public static function set"
            )
        else:
            declaration = (
                "public static function"
            )

        class_lines.extend([
            "",
            indent_text(
                render_function(
                    abc,
                    trait.method_index,
                    declaration,
                    method_name,
                    "void" if trait.kind == 3 else "*",
                    "value:*" if trait.kind == 3 else "",
                ),
                1,
            ),
        ])

    class_lines.append("}")

    if package_name:
        source = "\n".join([
            f"package {package_name}",
            "{",
            indent_text(
                "\n".join(class_lines),
                1,
            ),
            "}",
            "",
        ])
    else:
        source = (
            "\n".join(class_lines)
            + "\n"
        )

    destination = (
        OUTPUT_ROOT
        / Path(*package_name.split("."))
        / f"{class_name}.as"
    )

    destination.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    destination.write_text(source)

    print(f"✓ Wrote {destination}")


if __name__ == "__main__":
    main()
