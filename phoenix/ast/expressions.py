from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Expression:
    pass


@dataclass
class Constant(Expression):
    value: object


@dataclass
class Variable(Expression):
    name: str


@dataclass
class LexicalReference(Expression):
    name: str


@dataclass
class Unary(Expression):
    operator: str
    operand: Expression


@dataclass
class Binary(Expression):
    operator: str
    left: Expression
    right: Expression


@dataclass
class Property(Expression):
    receiver: Expression
    name: str


@dataclass
class Call(Expression):
    callee: Expression
    arguments: list[Expression] = field(default_factory=list)


@dataclass
class Construct(Expression):
    constructor: Expression
    arguments: list[Expression] = field(default_factory=list)


@dataclass
class Assignment(Expression):
    target: Expression
    value: Expression


@dataclass
class QualifiedProperty(Expression):
    owner: str
    name: str


@dataclass
class ScopeReference(Expression):
    name: str


@dataclass
class ScopeObject(Expression):
    index: int


@dataclass
class Slot(Expression):
    receiver: Expression
    slot: int


@dataclass
class FunctionReference(Expression):
    method_index: int


@dataclass
class ArrayLiteral(Expression):
    items: list[Expression] = field(default_factory=list)


@dataclass
class ObjectLiteral(Expression):
    entries: list[tuple[Expression, Expression]] = field(default_factory=list)


@dataclass
class ActivationObject(Expression):
    pass


@dataclass
class GlobalScope(Expression):
    pass


@dataclass
class DynamicProperty(Expression):
    receiver: Expression
    key: Expression
