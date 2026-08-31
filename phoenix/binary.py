from __future__ import annotations

import io
import struct
from typing import BinaryIO


class BinaryReader:
    def __init__(self, source: bytes | bytearray | BinaryIO):
        if isinstance(source, (bytes, bytearray)):
            self._stream = io.BytesIO(source)
        else:
            self._stream = source

    @property
    def position(self) -> int:
        return self._stream.tell()

    def seek(self, offset: int, whence: int = io.SEEK_SET) -> int:
        return self._stream.seek(offset, whence)

    def skip(self, count: int) -> int:
        return self.seek(count, io.SEEK_CUR)

    def read(self, size: int) -> bytes:
        if size < 0:
            raise ValueError("Read size cannot be negative.")

        data = self._stream.read(size)

        if len(data) != size:
            raise EOFError(
                f"Expected {size} bytes at offset {self.position - len(data)}, "
                f"but only received {len(data)}."
            )

        return data

    def u8(self) -> int:
        return struct.unpack("<B", self.read(1))[0]

    def s8(self) -> int:
        return struct.unpack("<b", self.read(1))[0]

    def u16(self) -> int:
        return struct.unpack("<H", self.read(2))[0]

    def s16(self) -> int:
        return struct.unpack("<h", self.read(2))[0]

    def u32(self) -> int:
        return struct.unpack("<I", self.read(4))[0]

    def s32(self) -> int:
        return struct.unpack("<i", self.read(4))[0]

    def u64(self) -> int:
        return struct.unpack("<Q", self.read(8))[0]

    def s64(self) -> int:
        return struct.unpack("<q", self.read(8))[0]

    def f32(self) -> float:
        return struct.unpack("<f", self.read(4))[0]

    def f64(self) -> float:
        return struct.unpack("<d", self.read(8))[0]

    def string(self, length: int, encoding: str = "utf-8") -> str:
        return self.read(length).decode(encoding)

    def cstring(self, encoding: str = "utf-8") -> str:
        data = bytearray()

        while True:
            byte = self.read(1)

            if byte == b"\x00":
                return data.decode(encoding)

            data.extend(byte)

    def peek(self, size: int) -> bytes:
        position = self.position

        try:
            return self.read(size)
        finally:
            self.seek(position)
