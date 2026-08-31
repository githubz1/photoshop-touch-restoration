from __future__ import annotations

import zlib
from dataclasses import dataclass

from phoenix.binary import BinaryReader


@dataclass
class SWF:
    """
    Represents an SWF file in uncompressed form.
    """
    signature: str
    version: int
    file_length: int
    data: bytes  # Complete uncompressed SWF (always starts with FWS)


def parse(data: bytes) -> SWF:
    """
    Parse an SWF.

    Supports:
        - FWS (uncompressed)
        - CWS (zlib compressed)

    Detects:
        - ZWS (LZMA), but does not yet implement it.

    Returns a complete, uncompressed SWF.
    """

    if len(data) < 8:
        raise ValueError("SWF file is too small.")

    reader = BinaryReader(data)

    signature = reader.string(3)

    if signature not in ("FWS", "CWS", "ZWS"):
        raise ValueError(f"Invalid SWF signature: {signature!r}")

    version = reader.u8()

    length_bytes = reader.read(4)
    file_length = int.from_bytes(length_bytes, "little")

    compressed_body = reader.read(len(data) - 8)

    if signature == "FWS":
        swf_data = data

    elif signature == "CWS":
        try:
            body = zlib.decompress(compressed_body)
        except zlib.error as exc:
            raise ValueError("Invalid zlib-compressed SWF.") from exc

        swf_data = (
            b"FWS"
            + bytes([version])
            + length_bytes
            + body
        )

    else:
        raise NotImplementedError(
            "LZMA-compressed SWFs (ZWS) are not supported yet."
        )

    if len(swf_data) != file_length:
        raise ValueError(
            f"Header declares {file_length} bytes, "
            f"but reconstructed SWF is {len(swf_data)} bytes."
        )

    return SWF(
        signature=signature,
        version=version,
        file_length=file_length,
        data=swf_data,
    )
