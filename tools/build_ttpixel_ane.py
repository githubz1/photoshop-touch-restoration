#!/usr/bin/env python3

from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_STORED, ZIP_DEFLATED
import hashlib
import xml.etree.ElementTree as ET


PROJECT_ROOT = Path(__file__).resolve().parent.parent
ROOT = PROJECT_ROOT / "output/ane/ttpixel"
ARM64 = ROOT / "arm64"

EXTENSION_XML = ARM64 / "extension-ios.xml"
CATALOG_XML = ROOT / "catalog.xml"
LIBRARY_SWF = ROOT / "library.swf"
NATIVE_LIB = ARM64 / "libTTPixelExtensionCocoaTouch.a"
PLATFORM_XML = ARM64 / "platform.xml"

OUTPUT_ANE = ARM64 / "TTPixel-arm64.ane"

MIMETYPE = b"application/vnd.adobe.air-native-extension-package+zip"


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verify_inputs() -> None:
    required = [
        EXTENSION_XML,
        CATALOG_XML,
        LIBRARY_SWF,
        NATIVE_LIB,
        PLATFORM_XML,
    ]

    for path in required:
        if not path.is_file():
            raise SystemExit(f"ERROR: missing input: {path}")


def verify_catalog() -> str:
    root = ET.parse(CATALOG_XML).getroot()

    library = next(
        (
            elem
            for elem in root.iter()
            if elem.tag.rsplit("}", 1)[-1] == "library"
            and elem.attrib.get("path") == "library.swf"
        ),
        None,
    )

    if library is None:
        raise SystemExit(
            "ERROR: catalog.xml has no library path=\"library.swf\" entry"
        )

    digest = next(
        (
            elem.attrib.get("value")
            for elem in library.iter()
            if elem.tag.rsplit("}", 1)[-1] == "digest"
            and elem.attrib.get("type") == "SHA-256"
        ),
        None,
    )

    if digest is None:
        raise SystemExit(
            "ERROR: catalog.xml has no SHA-256 digest for library.swf"
        )

    actual = sha256(LIBRARY_SWF.read_bytes())

    if digest.lower() != actual.lower():
        raise SystemExit(
            "ERROR: catalog/library.swf digest mismatch\n"
            f"catalog: {digest}\n"
            f"actual : {actual}"
        )

    return actual


def build_ane() -> None:
    if OUTPUT_ANE.exists():
        OUTPUT_ANE.unlink()

    with ZipFile(OUTPUT_ANE, "w") as z:
        # Canonical ANE mimetype: first entry and uncompressed.
        info = ZipInfo("mimetype")
        info.compress_type = ZIP_STORED
        z.writestr(info, MIMETYPE)

        z.write(
            EXTENSION_XML,
            "META-INF/ANE/extension.xml",
            compress_type=ZIP_DEFLATED,
        )

        z.write(
            CATALOG_XML,
            "catalog.xml",
            compress_type=ZIP_DEFLATED,
        )

        z.write(
            LIBRARY_SWF,
            "library.swf",
            compress_type=ZIP_STORED,
        )

        z.write(
            LIBRARY_SWF,
            "META-INF/ANE/iPhone-ARM/library.swf",
            compress_type=ZIP_STORED,
        )

        z.write(
            NATIVE_LIB,
            "META-INF/ANE/iPhone-ARM/libTTPixelExtensionCocoaTouch.a",
            compress_type=ZIP_DEFLATED,
        )

        z.write(
            PLATFORM_XML,
            "META-INF/ANE/iPhone-ARM/platform.xml",
            compress_type=ZIP_DEFLATED,
        )


def verify_ane(source_swf_digest: str) -> None:
    expected_entries = [
        "mimetype",
        "META-INF/ANE/extension.xml",
        "catalog.xml",
        "library.swf",
        "META-INF/ANE/iPhone-ARM/library.swf",
        "META-INF/ANE/iPhone-ARM/libTTPixelExtensionCocoaTouch.a",
        "META-INF/ANE/iPhone-ARM/platform.xml",
    ]

    with ZipFile(OUTPUT_ANE) as z:
        names = z.namelist()

        if names != expected_entries:
            raise SystemExit(
                "ERROR: unexpected ANE entry layout\n"
                f"expected: {expected_entries}\n"
                f"actual  : {names}"
            )

        mimetype_info = z.getinfo("mimetype")

        if mimetype_info.compress_type != ZIP_STORED:
            raise SystemExit("ERROR: ANE mimetype is compressed")

        if z.read("mimetype") != MIMETYPE:
            raise SystemExit("ERROR: incorrect ANE mimetype")

        root_swf = z.read("library.swf")
        ios_swf = z.read("META-INF/ANE/iPhone-ARM/library.swf")

        if root_swf != LIBRARY_SWF.read_bytes():
            raise SystemExit(
                "ERROR: root library.swf differs from source"
            )

        if ios_swf != root_swf:
            raise SystemExit(
                "ERROR: iPhone-ARM/library.swf differs from root library.swf"
            )

        if sha256(root_swf) != source_swf_digest:
            raise SystemExit(
                "ERROR: embedded SWF digest changed during packaging"
            )

        if (
            z.read("META-INF/ANE/iPhone-ARM/platform.xml")
            != PLATFORM_XML.read_bytes()
        ):
            raise SystemExit(
                "ERROR: embedded platform.xml differs from source"
            )

        if (
            z.read(
                "META-INF/ANE/iPhone-ARM/"
                "libTTPixelExtensionCocoaTouch.a"
            )
            != NATIVE_LIB.read_bytes()
        ):
            raise SystemExit(
                "ERROR: embedded native archive differs from source"
            )

        print("\n=== CONTENTS ===")
        for index, info in enumerate(z.infolist()):
            print(
                f"{index:2d}  "
                f"method={info.compress_type:<2d}  "
                f"size={info.file_size:<8,d}  "
                f"compressed={info.compress_size:<8,d}  "
                f"{info.filename}"
            )


def main() -> None:
    verify_inputs()
    source_swf_digest = verify_catalog()

    build_ane()
    verify_ane(source_swf_digest)

    print("\n=== RESULT ===")
    print(f"ANE    : {OUTPUT_ANE}")
    print(f"Size   : {OUTPUT_ANE.stat().st_size:,} bytes")
    print(f"SHA256 : {sha256(OUTPUT_ANE.read_bytes())}")
    print(f"SWF    : {LIBRARY_SWF.stat().st_size:,} bytes")
    print(f"SWF256 : {source_swf_digest}")
    print("Status : OK")


if __name__ == "__main__":
    main()
