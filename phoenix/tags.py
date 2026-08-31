from __future__ import annotations

from dataclasses import dataclass

from phoenix.binary import BinaryReader


TAG_NAMES = {
    0: "End",
    1: "ShowFrame",
    2: "DefineShape",
    9: "SetBackgroundColor",
    12: "DoAction",
    20: "DefineBitsLossless",
    21: "DefineBitsJPEG2",
    22: "DefineShape2",
    26: "PlaceObject2",
    28: "RemoveObject2",
    32: "DefineShape3",
    35: "DefineBitsJPEG3",
    36: "DefineBitsLossless2",
    37: "DefineEditText",
    39: "DefineSprite",
    41: "ProductInfo",
    43: "FrameLabel",
    46: "DefineMorphShape",
    56: "ExportAssets",
    57: "ImportAssets",
    65: "ScriptLimits",
    69: "FileAttributes",
    70: "PlaceObject3",
    73: "DefineFontAlignZones",
    74: "CSMTextSettings",
    75: "DefineFont3",
    76: "SymbolClass",
    77: "Metadata",
    78: "DefineScalingGrid",
    82: "DoABC",
    83: "DefineShape4",
    84: "DefineMorphShape2",
    86: "DefineSceneAndFrameLabelData",
    87: "DefineBinaryData",
    88: "DefineFontName",
    90: "DefineBitsJPEG4",
}


def tag_name(code: int) -> str:
    return TAG_NAMES.get(code, f"Unknown({code})")


@dataclass
class Tag:
    code: int
    length: int
    offset: int
    data: bytes


@dataclass
class DoABC:
    flags: int
    name: str
    abc: bytes


def parse_doabc(tag: Tag) -> DoABC:
    if tag.code != 82:
        raise ValueError(f"Tag {tag.code} is not a DoABC tag.")

    if len(tag.data) < 5:
        raise ValueError("DoABC payload is too short.")

    reader = BinaryReader(tag.data)

    flags = reader.u32()
    name = reader.cstring()
    abc = reader.read(len(tag.data) - reader.position)

    if len(abc) < 4:
        raise ValueError("Extracted ABC payload is too short.")

    return DoABC(
        flags=flags,
        name=name,
        abc=abc,
    )


def parse_tags(swf_data: bytes) -> list[Tag]:
    """
    Parse all SWF tags.

    Expects a complete, uncompressed FWS file.
    """

    reader = BinaryReader(swf_data)

    reader.skip(3)
    reader.skip(1)
    reader.skip(4)

    first = reader.u8()
    nbits = first >> 3

    total_bits = 5 + nbits * 4
    rect_bytes = (total_bits + 7) // 8

    reader.skip(rect_bytes - 1)

    reader.skip(4)

    tags = []

    while True:
        offset = reader.position
        header = reader.u16()

        code = header >> 6
        length = header & 0x3F

        if length == 0x3F:
            length = reader.u32()

        data = reader.read(length)

        tags.append(
            Tag(
                code=code,
                length=length,
                offset=offset,
                data=data,
            )
        )

        if code == 0:
            break

    return tags
