from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Node:
    pass


@dataclass
class BlockNode(Node):
    statements: list[Node] = field(default_factory=list)


@dataclass
class WhileNode(Node):
    condition: object = None
    body: BlockNode = field(default_factory=BlockNode)


@dataclass
class IfNode(Node):
    condition: object = None
    then_body: BlockNode = field(default_factory=BlockNode)
    else_body: BlockNode | None = None


@dataclass
class ForInNode(Node):
    target: object = None
    iterable: object = None
    body: BlockNode = field(default_factory=BlockNode)


@dataclass
class InstructionNode(Node):
    block: object
