from __future__ import annotations

from phoenix.ast.expressions import (
    DynamicProperty,
    ObjectLiteral,
    GlobalScope,
    ArrayLiteral,
    ActivationObject,
    Assignment,
    Binary,
    Call,
    Constant,
    FunctionReference,
    LexicalReference,
    Construct,
    Property,
    QualifiedProperty,
    ScopeReference,
    ScopeObject,
    Slot,
    Unary,
    Variable,
)
from phoenix.formatter import Formatter


_BINARY_OPERATORS = {
    "add": "+",
    "subtract": "-",
    "multiply": "*",
    "divide": "/",
    "modulo": "%",
    "lshift": "<<",
    "rshift": ">>",
    "urshift": ">>>",
    "bitand": "&",
    "bitor": "|",
    "bitxor": "^",
    "equals": "==",
    "strictequals": "===",
    "lessthan": "<",
    "lessequals": "<=",
    "greaterthan": ">",
    "greaterequals": ">=",
    "instanceof": "instanceof",
    "in": "in",
}

_UNARY_OPERATORS = {
    "negate": "-",
    "negate_i": "-",
    "not": "!",
    "bitnot": "~",
    "typeof": "typeof ",
}


class StackSimulator:

    def __init__(self, abc=None):
        self.stack = []
        self.statements = []
        self.branch_condition = None
        self.abc = abc
        self.formatter = Formatter(abc) if abc is not None else None

    def push(self, value):
        self.stack.append(value)

    def pop(self):
        if not self.stack:
            raise IndexError("AVM2 operand stack underflow")
        return self.stack.pop()

    def depth(self):
        return len(self.stack)

    def _local_index(self, instruction):
        shortcuts = {
            "getlocal0": 0,
            "getlocal1": 1,
            "getlocal2": 2,
            "getlocal3": 3,
            "setlocal0": 0,
            "setlocal1": 1,
            "setlocal2": 2,
            "setlocal3": 3,
        }

        if instruction.mnemonic in shortcuts:
            return shortcuts[instruction.mnemonic]

        return instruction.operands[0]

    def _multiname(self, index):
        if (
            self.abc is not None
            and 0 <= index < len(self.abc.multinames)
        ):
            return self.formatter.multiname(
                self.abc.multinames[index]
            )

        return f"<multiname:{index}>"

    def _arguments(self, count):
        arguments = [self.pop() for _ in range(count)]
        arguments.reverse()
        return arguments

    def execute(self, instruction):
        mnemonic = instruction.mnemonic
        operands = instruction.operands

        if mnemonic == "pushtrue":
            self.push(Constant(True))

        elif mnemonic == "pushfalse":
            self.push(Constant(False))

        elif mnemonic == "pushnull":
            self.push(Constant(None))

        elif mnemonic == "pushundefined":
            self.push(Constant("undefined"))

        elif mnemonic == "pushnan":
            self.push(Constant(float("nan")))

        elif mnemonic in {"pushbyte", "pushshort"}:
            self.push(Constant(operands[0]))

        elif mnemonic == "pushint":
            index = operands[0]

            if (
                self.abc is not None
                and 0 <= index < len(self.abc.ints)
            ):
                value = self.abc.ints[index]
            else:
                value = f"<int:{index}>"

            self.push(Constant(value))

        elif mnemonic == "pushdouble":
            index = operands[0]

            if (
                self.abc is not None
                and 0 <= index < len(self.abc.doubles)
            ):
                value = self.abc.doubles[index]
            else:
                value = f"<double:{index}>"

            self.push(Constant(value))

        elif mnemonic == "pushstring":
            index = operands[0]

            if (
                self.abc is not None
                and 0 <= index < len(self.abc.strings)
            ):
                self.push(Constant(self.abc.strings[index]))
            else:
                self.push(Constant(f"<string:{index}>"))

        elif mnemonic == "getlex":
            self.push(
                LexicalReference(
                    self._multiname(operands[0])
                )
            )

        elif mnemonic == "newfunction":
            self.push(
                FunctionReference(
                    operands[0]
                )
            )

        elif mnemonic == "newarray":
            count = operands[0]
            items = self._arguments(count)

            self.push(
                ArrayLiteral(items)
            )

        elif mnemonic == "newobject":
            count = operands[0]
            entries = []

            for _ in range(count):
                value = self.pop()
                name = self.pop()
                entries.append((name, value))

            entries.reverse()

            self.push(
                ObjectLiteral(entries)
            )

        elif mnemonic == "newactivation":
            self.push(
                ActivationObject()
            )

        elif mnemonic == "getglobalscope":
            self.push(
                GlobalScope()
            )

        elif mnemonic in {"findproperty", "findpropstrict"}:
            self.push(
                ScopeReference(
                    self._multiname(operands[0])
                )
            )

        elif mnemonic == "getscopeobject":
            self.push(
                ScopeObject(
                    operands[0]
                )
            )

        elif mnemonic == "getslot":
            receiver = self.pop()

            self.push(
                Slot(
                    receiver,
                    operands[0],
                )
            )

        elif mnemonic == "setslot":
            value = self.pop()
            receiver = self.pop()

            self.statements.append(
                Assignment(
                    Slot(
                        receiver,
                        operands[0],
                    ),
                    value,
                )
            )

        elif mnemonic.startswith("getlocal"):
            index = self._local_index(instruction)
            self.push(Variable(f"local{index}"))

        elif mnemonic.startswith("setlocal"):
            index = self._local_index(instruction)
            value = self.pop()

            self.statements.append(
                Assignment(
                    Variable(f"local{index}"),
                    value,
                )
            )

        elif mnemonic == "dup":
            value = self.pop()
            self.push(value)
            self.push(value)

        elif mnemonic == "swap":
            right = self.pop()
            left = self.pop()
            self.push(right)
            self.push(left)

        elif mnemonic == "pop":
            self.statements.append(self.pop())

        elif mnemonic in _BINARY_OPERATORS:
            right = self.pop()
            left = self.pop()

            self.push(
                Binary(
                    _BINARY_OPERATORS[mnemonic],
                    left,
                    right,
                )
            )

        elif mnemonic in _UNARY_OPERATORS:
            self.push(
                Unary(
                    _UNARY_OPERATORS[mnemonic],
                    self.pop(),
                )
            )

        elif mnemonic in {
            "coerce",
            "coerce_a",
            "coerce_s",
            "convert_b",
            "convert_d",
            "convert_i",
            "convert_o",
            "convert_s",
            "convert_u",
            "astype",
            "astypelate",
        }:
            pass

        elif mnemonic == "getproperty":
            multiname_index = operands[0]

            kind = None

            if (
                self.abc is not None
                and 0 <= multiname_index < len(self.abc.multinames)
            ):
                kind = self.abc.multinames[multiname_index].kind

            # Runtime-name multinames consume the property key
            # from the operand stack.
            if kind in {0x11, 0x12, 0x1B, 0x1C}:
                key = self.pop()
                receiver = self.pop()

                self.push(
                    DynamicProperty(
                        receiver,
                        key,
                    )
                )

            else:
                name = self._multiname(multiname_index)
                receiver = self.pop()

                if isinstance(receiver, LexicalReference):
                    self.push(
                        QualifiedProperty(
                            receiver.name,
                            name,
                        )
                    )
                else:
                    self.push(
                        Property(
                            receiver,
                            name,
                        )
                    )

        elif mnemonic in {"setproperty", "initproperty"}:
            name = self._multiname(operands[0])
            value = self.pop()
            receiver = self.pop()

            self.statements.append(
                Assignment(
                    Property(receiver, name),
                    value,
                )
            )

        elif mnemonic in {
            "callproperty",
            "callproplex",
            "callpropvoid",
        }:
            name_index = operands[0]
            argument_count = operands[1]

            arguments = self._arguments(argument_count)
            receiver = self.pop()

            call = Call(
                Property(
                    receiver,
                    self._multiname(name_index),
                ),
                arguments,
            )

            if mnemonic == "callpropvoid":
                self.statements.append(call)
            else:
                self.push(call)

        elif mnemonic == "constructprop":
            name_index = operands[0]
            argument_count = operands[1]

            arguments = self._arguments(argument_count)
            receiver = self.pop()

            self.push(
                Construct(
                    Property(
                        receiver,
                        self._multiname(name_index),
                    ),
                    arguments,
                )
            )

        elif mnemonic == "iftrue":
            self.branch_condition = self.pop()

        elif mnemonic == "iffalse":
            self.branch_condition = Unary(
                "!",
                self.pop(),
            )

        elif mnemonic in {
            "ifeq",
            "ifne",
            "ifstricteq",
            "ifstrictne",
            "iflt",
            "ifle",
            "ifgt",
            "ifge",
            "ifnlt",
            "ifnle",
            "ifngt",
            "ifnge",
        }:
            right = self.pop()
            left = self.pop()

            operators = {
                "ifeq": "==",
                "ifne": "!=",
                "ifstricteq": "===",
                "ifstrictne": "!==",
                "iflt": "<",
                "ifle": "<=",
                "ifgt": ">",
                "ifge": ">=",
                "ifnlt": ">=",
                "ifnle": ">",
                "ifngt": "<=",
                "ifnge": "<",
            }

            self.branch_condition = Binary(
                operators[mnemonic],
                left,
                right,
            )

        elif mnemonic == "returnvalue":
            self.statements.append(
                Unary("return ", self.pop())
            )

        elif mnemonic == "returnvoid":
            self.statements.append(Constant("return"))

        return self
