"""AVM2 opcode definitions."""

from dataclasses import dataclass
from typing import Tuple


@dataclass(frozen=True)
class OpcodeInfo:
    opcode: int
    mnemonic: str
    operands: Tuple[str, ...]


NONE: Tuple[str, ...] = ()
U30: Tuple[str, ...] = ("u30",)
S24: Tuple[str, ...] = ("s24",)
BYTE: Tuple[str, ...] = ("byte",)
U8: Tuple[str, ...] = ("u8",)


OPCODES = {
    0x02: ("nop", NONE),
    0x03: ("throw", NONE),

    0x04: ("getsuper", U30),
    0x05: ("setsuper", U30),

    0x00: ("bkpt", NONE),
    0x08: ("kill", U30),
    0x09: ("label", NONE),
    0x0C: ("ifnlt", S24),
    0x0D: ("ifnle", S24),
    0x0E: ("ifngt", S24),
    0x0F: ("ifnge", S24),
    0x13: ("ifeq", S24),
    0x14: ("ifne", S24),
    0x15: ("iflt", S24),
    0x16: ("ifle", S24),
    0x17: ("ifgt", S24),
    0x18: ("ifge", S24),
    0x19: ("ifstricteq", S24),
    0x1A: ("ifstrictne", S24),
    0x1B: ("lookupswitch", ("s24","u30")),
    0x1C: ("pushwith", NONE),
    0x1D: ("popscope", NONE),
    0x1E: ("nextname", NONE),
    0x1F: ("hasnext", NONE),
    0x10: ("jump", S24),
    0x11: ("iftrue", S24),
    0x12: ("iffalse", S24),
    0x20: ("pushnull", NONE),
    0x21: ("pushundefined", NONE),
    0x80: ("coerce", U30),
    0x5A: ("newcatch", U30),
    0x5D: ("findpropstrict", U30),
    0x5E: ("findproperty", U30),
    0x5F: ("finddef", U30),
    0x60: ("getlex", U30),
    0x61: ("setproperty", U30),
    0x64: ("getglobalscope", NONE),
    0x65: ("getscopeobject", U8),
    0x66: ("getproperty", U30),
    0x68: ("initproperty", U30),
    0x6A: ("deleteproperty", U30),
    0x6C: ("getslot", U30),
    0x6D: ("setslot", U30),
    0x6E: ("getglobalslot", U30),
    0x6F: ("setglobalslot", U30),
    0x22: ("nextname", NONE),
    0x23: ("hasnext", NONE),
    0x2C: ("pushstring", U30),
    0x2D: ("pushint", U30),
    0x2E: ("pushuint", U30),
    0x2F: ("pushdouble", U30),
    0x31: ("pushnamespace", U30),
    0x4B: ("callsuperid", (U30[0], U30[0])),
    0x4C: ("callproplex", (U30[0], U30[0])),
    0x4E: ("callsupervoid", (U30[0], U30[0])),
    0x4F: ("callpropvoid", (U30[0], U30[0])),
    0x50: ("sxi1", NONE),
    0x51: ("sxi8", NONE),
    0x52: ("sxi16", NONE),
    0x70: ("convert_s", NONE),
    0x71: ("esc_xelem", NONE),
    0x72: ("esc_xattr", NONE),
    0x73: ("convert_i", NONE),
    0x74: ("convert_u", NONE),
    0x75: ("convert_d", NONE),
    0x76: ("convert_b", NONE),
    0x77: ("convert_o", NONE),
    0x78: ("checkfilter", NONE),
    0x82: ("coerce_a", NONE),
    0x85: ("coerce_s", NONE),
    0x90: ("negate", NONE),
    0x91: ("increment", NONE),
    0x93: ("decrement", NONE),
    0x95: ("typeof", NONE),
    0x96: ("not", NONE),
    0x97: ("bitnot", NONE),
    0xA0: ("add", NONE),
    0xA1: ("subtract", NONE),
    0xA2: ("multiply", NONE),
    0xA3: ("divide", NONE),
    0xA4: ("modulo", NONE),
    0xA5: ("lshift", NONE),
    0xA6: ("rshift", NONE),
    0xA7: ("urshift", NONE),
    0xA8: ("bitand", NONE),
    0xA9: ("bitor", NONE),
    0xAA: ("bitxor", NONE),
    0xAB: ("equals", NONE),
    0xAC: ("strictequals", NONE),
    0xAD: ("lessthan", NONE),
    0xAE: ("lessequals", NONE),
    0xAF: ("greaterthan", NONE),
    0xB0: ("greaterequals", NONE),
    0xB1: ("instanceof", NONE),
    0xB3: ("istypelate", NONE),
    0xB4: ("in", NONE),
    0xC0: ("increment_i", NONE),
    0xC1: ("decrement_i", NONE),
    0xC4: ("negate_i", NONE),
    0xC5: ("add_i", NONE),
    0xC6: ("subtract_i", NONE),
    0xC7: ("multiply_i", NONE),


    0x24: ("pushbyte", BYTE),
    0x25: ("pushshort", U30),
    0x26: ("pushtrue", NONE),
    0x27: ("pushfalse", NONE),
    0x28: ("pushnan", NONE),

    0x29: ("pop", NONE),
    0x2A: ("dup", NONE),
    0x2B: ("swap", NONE),

    0x30: ("pushscope", NONE),

    0x47: ("returnvoid", NONE),
    0x40: ("newfunction", U30),
    0x41: ("call", U30),
    0x42: ("construct", U30),
    0x43: ("callmethod", (U30[0], U30[0])),
    0x44: ("callstatic", (U30[0], U30[0])),
    0x45: ("callsuper", (U30[0], U30[0])),
    0x46: ("callproperty", (U30[0], U30[0])),
    0x49: ("constructsuper", U30),
    0x4A: ("constructprop", (U30[0], U30[0])),
    0x48: ("returnvalue", NONE),

    0x62: ("getlocal", U30),
    0x63: ("setlocal", U30),

    0xC2: ("inclocal", U30),
    0xC3: ("declocal", U30),
    0xC8: ("inclocal_i", U30),
    0xC9: ("declocal_i", U30),
    0xD0: ("getlocal0", NONE),
    0xD1: ("getlocal1", NONE),
    0xD2: ("getlocal2", NONE),
    0xD3: ("getlocal3", NONE),

    0xD4: ("setlocal0", NONE),
    0xD5: ("setlocal1", NONE),
    0xD6: ("setlocal2", NONE),
    0xD7: ("setlocal3", NONE),

    0x32: ("hasnext2", (U30[0], U30[0])),
    0x53: ("applytype", U30),
    0x55: ("newobject", U30),
    0x56: ("newarray", U30),
    0x57: ("newactivation", NONE),
    0x58: ("newclass", U30),
    0x59: ("newcatch", U30),
    0x87: ("astypelate", NONE),
}


OPERAND_NONE = "none"
OPERAND_U30 = "u30"
OPERAND_S24 = "s24"
OPERAND_U8 = "u8"
