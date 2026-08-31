BOOKKEEPING = {
    "label",
    "jump",
    "lookupswitch",
    "iftrue",
    "iffalse",
    "ifeq",
    "ifne",
    "iflt",
    "ifle",
    "ifgt",
    "ifge",
    "ifnlt",
    "ifnle",
    "ifngt",
    "ifnge",
    "ifstrictne",
    "ifstricteq",
    "pushscope",
    "popscope",
}

TYPE_ONLY = {
    "coerce",
    "coerce_a",
    "coerce_s",
    "convert_b",
    "convert_d",
    "convert_i",
    "convert_s",
    "convert_u",
    "astypelate",
}

SEMANTIC = "semantic"
BOOKKEEPING_KIND = "bookkeeping"
TYPE_KIND = "type"
UNKNOWN = "unknown"


def classify(opcode):
    if opcode in BOOKKEEPING:
        return BOOKKEEPING_KIND

    if opcode in TYPE_ONLY:
        return TYPE_KIND

    return SEMANTIC
