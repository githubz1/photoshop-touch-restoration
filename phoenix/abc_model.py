from __future__ import annotations

from dataclasses import dataclass, field, field
from typing import List, Optional


@dataclass
class Namespace:
    kind: int
    name_index: int


@dataclass
class NamespaceSet:
    namespaces: List[int]


@dataclass
class Multiname:
    kind: int
    data: dict



@dataclass
class MethodInfo:
    parameter_types: list[int]
    return_type: int
    name_index: int
    flags: int
    optional_values: list[int] = field(default_factory=list)
    parameter_names: list[int] = field(default_factory=list)




@dataclass
class MetadataInfo:
    name_index: int
    items: list[tuple[int, int]] = field(default_factory=list)



@dataclass
class TraitInfo:
    name_index: int
    kind: int
    attributes: int
    slot_id: int = 0
    type_name: int = 0
    value_index: int = 0
    value_kind: int = 0
    class_index: int = 0
    function_index: int = 0
    disp_id: int = 0
    method_index: int = 0
    metadata: list[int] = field(default_factory=list)


@dataclass
class InstanceInfo:
    name_index: int
    super_name_index: int
    flags: int
    protected_ns: int
    interfaces: list[int] = field(default_factory=list)
    iinit: int = 0
    trait_count: int = 0
    traits: list[TraitInfo] = field(default_factory=list)



@dataclass
class ClassInfo:
    cinit: int
    trait_count: int = 0
    traits: list[TraitInfo] = field(default_factory=list)

@dataclass
class ScriptInfo:
    init: int
    trait_count: int = 0
    traits: list[TraitInfo] = field(default_factory=list)


@dataclass
class Instruction:
    offset: int
    opcode: int
    mnemonic: str
    operands: list[int] = field(default_factory=list)


@dataclass
class ExceptionInfo:
    from_offset: int
    to_offset: int
    target_offset: int
    exception_type: int
    variable_name: int


@dataclass
class MethodBodyInfo:
    method_index: int
    max_stack: int
    local_count: int
    init_scope_depth: int
    max_scope_depth: int
    code: bytes = b""
    instructions: list[Instruction] = field(default_factory=list)
    exceptions: list[ExceptionInfo] = field(default_factory=list)
    traits: list[TraitInfo] = field(default_factory=list)


@dataclass
class ConstantPool:
    ints: List[int] = field(default_factory=lambda: [0])
    uints: List[int] = field(default_factory=lambda: [0])
    doubles: List[float] = field(default_factory=lambda: [0.0])
    floats: List[float] = field(default_factory=lambda: [0.0])
    strings: List[str] = field(default_factory=lambda: [""])
    namespaces: List[Optional[Namespace]] = field(
        default_factory=lambda: [None]
    )
    namespace_sets: List[Optional[NamespaceSet]] = field(
        default_factory=lambda: [None]
    )
    multinames: List[Optional[Multiname]] = field(
        default_factory=lambda: [None]
    )


@dataclass
class ABCModel:
    minor: int = 0
    major: int = 0

    pool: ConstantPool = field(default_factory=ConstantPool)

    namespace_count: int = 0
    namespace_set_count: int = 0
    multiname_count: int = 0
    method_count: int = 0
    metadata_count: int = 0
    instance_count: int = 0
    class_count: int = 0
    script_count: int = 0
    method_body_count: int = 0
    method_bodies: list[MethodBodyInfo] = field(default_factory=list)
    scripts: list[ScriptInfo] = field(default_factory=list)
    classes: list[ClassInfo] = field(default_factory=list)
    instances: list[InstanceInfo] = field(default_factory=list)
    metadata: list[MetadataInfo] = field(default_factory=list)
    methods: list[MethodInfo] = field(default_factory=list)
