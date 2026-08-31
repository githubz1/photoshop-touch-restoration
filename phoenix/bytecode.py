"""
Bytecode reader for AVM2 method bodies.
"""

class BytecodeReader:

    def __init__(self, data: bytes):
        self.data = data
        self.offset = 0

    @property
    def eof(self):
        return self.offset >= len(self.data)

    def tell(self):
        return self.offset

    def read_u8(self):
        if self.offset >= len(self.data):
            raise EOFError("Unexpected end of bytecode")

        value = self.data[self.offset]
        self.offset += 1
        return value

    def read_bytes(self, count):
        if self.offset + count > len(self.data):
            raise EOFError("Unexpected end of bytecode")

        value = self.data[self.offset:self.offset + count]
        self.offset += count
        return value

    def read_s24(self):
        b0 = self.read_u8()
        b1 = self.read_u8()
        b2 = self.read_u8()

        value = b0 | (b1 << 8) | (b2 << 16)

        if value & 0x800000:
            value -= 1 << 24

        return value

    def read_u30(self):
        value = 0

        for shift in (0, 7, 14, 21, 28):
            byte = self.read_u8()
            value |= (byte & 0x7F) << shift

            if not (byte & 0x80):
                return value

        raise ValueError("Invalid U30")
