from __future__ import annotations

import struct

from phoenix.abc_model import (
    ABCModel,
    Multiname,
    Namespace,
    NamespaceSet,
    MethodInfo,
    MetadataInfo,
    TraitInfo,
    InstanceInfo,
    ClassInfo,
    ScriptInfo,
    ExceptionInfo,
    MethodBodyInfo,
)


class BinaryReader:
    def __init__(self, data: bytes):
        self.data = data
        self.offset = 0

    def read(self, size: int) -> bytes:
        if size < 0:
            raise ValueError("Read size cannot be negative")

        end = self.offset + size

        if end > len(self.data):
            raise EOFError(
                f"Unexpected end of data at offset 0x{self.offset:X}: "
                f"requested {size} byte(s), "
                f"only {len(self.data) - self.offset} remain"
            )

        value = self.data[self.offset:end]
        self.offset = end
        return value

    def read_u8(self) -> int:
        return self.read(1)[0]

    def read_u16_le(self) -> int:
        return struct.unpack("<H", self.read(2))[0]

    def read_f32_le(self) -> float:
        return struct.unpack("<f", self.read(4))[0]

    def read_f64_le(self) -> float:
        return struct.unpack("<d", self.read(8))[0]


class ABCParser:
    def __init__(self, data: bytes):
        self.reader = BinaryReader(data)
        self.model = ABCModel()

    @property
    def minor(self) -> int:
        return self.model.minor

    @property
    def major(self) -> int:
        return self.model.major

    @property
    def pool(self):
        return self.model.pool

    @property
    def namespace_count(self) -> int:
        return self.model.namespace_count

    @property
    def namespace_set_count(self) -> int:
        return self.model.namespace_set_count

    @property
    def multiname_count(self) -> int:
        return self.model.multiname_count

    def read_u30(self) -> int:
        value = 0

        for shift in range(0, 35, 7):
            byte = self.reader.read_u8()
            value |= (byte & 0x7F) << shift

            if (byte & 0x80) == 0:
                if value > 0x3FFFFFFF:
                    raise ValueError(
                        f"Invalid U30 value 0x{value:X} "
                        f"ending at offset 0x{self.reader.offset:X}"
                    )

                return value

        raise ValueError(
            f"Invalid unterminated U30 at "
            f"offset 0x{self.reader.offset:X}"
        )

    def read_u32(self) -> int:
        value = 0

        for shift in range(0, 35, 7):
            byte = self.reader.read_u8()
            value |= (byte & 0x7F) << shift

            if (byte & 0x80) == 0:
                return value & 0xFFFFFFFF

        raise ValueError(
            f"Invalid unterminated U32 at "
            f"offset 0x{self.reader.offset:X}"
        )

    def read_s32(self) -> int:
        value = self.read_u32()

        if value & 0x80000000:
            value -= 0x100000000

        return value

    def read_string(self) -> str:
        length = self.read_u30()
        raw = self.reader.read(length)

        try:
            return raw.decode("utf-8")
        except UnicodeDecodeError as error:
            raise ValueError(
                f"Invalid UTF-8 string ending at "
                f"offset 0x{self.reader.offset:X}"
            ) from error

    def parse_header(self):
        self.model.minor = self.reader.read_u16_le()
        self.model.major = self.reader.read_u16_le()
        return self

    def parse_integer_pool(self):
        count = self.read_u30()

        for _ in range(1, count):
            self.model.pool.ints.append(
                self.read_s32()
            )

        return self

    def parse_uint_pool(self):
        count = self.read_u30()

        for _ in range(1, count):
            self.model.pool.uints.append(
                self.read_u32()
            )

        return self

    def parse_double_pool(self):
        count = self.read_u30()

        for _ in range(1, count):
            self.model.pool.doubles.append(
                self.reader.read_f64_le()
            )

        return self

    def parse_float_pool(self):
        # ABC 47.16 introduced the float constant pool.
        if (self.major, self.minor) < (47, 16):
            return self

        count = self.read_u30()

        for _ in range(1, count):
            self.model.pool.floats.append(
                self.reader.read_f32_le()
            )

        return self

    def parse_string_pool(self):
        count = self.read_u30()

        for _ in range(1, count):
            self.model.pool.strings.append(
                self.read_string()
            )

        return self

    def parse_namespace_pool(self):
        count = self.read_u30()
        self.model.namespace_count = count

        valid_kinds = {
            0x05,  # PrivateNs
            0x08,  # Namespace
            0x16,  # PackageNamespace
            0x17,  # PackageInternalNs
            0x18,  # ProtectedNamespace
            0x19,  # ExplicitNamespace
            0x1A,  # StaticProtectedNs
        }

        for _ in range(1, count):
            kind_offset = self.reader.offset
            kind = self.reader.read_u8()

            if kind not in valid_kinds:
                raise ValueError(
                    f"Unknown namespace kind 0x{kind:02X} "
                    f"at offset 0x{kind_offset:X}"
                )

            name_index = self.read_u30()

            if name_index >= len(self.model.pool.strings):
                raise ValueError(
                    f"Namespace string index {name_index} "
                    f"is out of range at offset "
                    f"0x{self.reader.offset:X}"
                )

            self.model.pool.namespaces.append(
                Namespace(
                    kind=kind,
                    name_index=name_index,
                )
            )

        return self

    def parse_namespace_sets(self):
        count = self.read_u30()
        self.model.namespace_set_count = count

        for _ in range(1, count):
            ns_count = self.read_u30()
            members = []

            for _ in range(ns_count):
                ns_index = self.read_u30()

                if ns_index >= len(self.model.pool.namespaces):
                    raise ValueError(
                        f"Namespace index {ns_index} is out of range "
                        f"at offset 0x{self.reader.offset:X}"
                    )

                members.append(ns_index)

            self.model.pool.namespace_sets.append(
                NamespaceSet(namespaces=members)
            )

        return self



    def _validate_pool_index(
        self,
        index: int,
        pool,
        pool_name: str,
    ):
        if index == 0:
            return

        if index >= len(pool):
            raise ValueError(
                f"{pool_name} index {index} out of range"
            )

    def parse_multiname_pool(self):
        count = self.read_u30()
        self.model.multiname_count = count

        def validate_index(index, items, label):
            if index >= len(items):
                raise ValueError(
                    f"{label} index {index} is out of range "
                    f"at offset 0x{self.reader.offset:X}"
                )

        for _ in range(1, count):
            kind_offset = self.reader.offset
            kind = self.reader.read_u8()
            data = {}

            # QName / QNameA
            if kind in {0x07, 0x0D}:
                namespace_index = self.read_u30()
                name_index = self.read_u30()

                validate_index(
                    namespace_index,
                    self.model.pool.namespaces,
                    "Namespace",
                )
                validate_index(
                    name_index,
                    self.model.pool.strings,
                    "String",
                )

                data = {
                    "namespace_index": namespace_index,
                    "name_index": name_index,
                }

            # RTQName / RTQNameA
            elif kind in {0x0F, 0x10}:
                name_index = self.read_u30()

                validate_index(
                    name_index,
                    self.model.pool.strings,
                    "String",
                )

                data = {
                    "name_index": name_index,
                }

            # RTQNameL / RTQNameLA
            elif kind in {0x11, 0x12}:
                data = {}

            # Multiname / MultinameA
            elif kind in {0x09, 0x0E}:
                name_index = self.read_u30()
                namespace_set_index = self.read_u30()

                validate_index(
                    name_index,
                    self.model.pool.strings,
                    "String",
                )
                validate_index(
                    namespace_set_index,
                    self.model.pool.namespace_sets,
                    "Namespace-set",
                )

                data = {
                    "name_index": name_index,
                    "namespace_set_index": namespace_set_index,
                }

            # MultinameL / MultinameLA
            elif kind in {0x1B, 0x1C}:
                namespace_set_index = self.read_u30()

                validate_index(
                    namespace_set_index,
                    self.model.pool.namespace_sets,
                    "Namespace-set",
                )

                data = {
                    "namespace_set_index": namespace_set_index,
                }

            # TypeName
            elif kind == 0x1D:
                qname_index = self.read_u30()
                parameter_count = self.read_u30()

                if qname_index >= count:
                    raise ValueError(
                        f"Multiname index {qname_index} is out of range "
                        f"at offset 0x{self.reader.offset:X}"
                    )

                parameter_indices = []

                for _ in range(parameter_count):
                    parameter_index = self.read_u30()

                    if parameter_index >= count:
                        raise ValueError(
                            f"Multiname index {parameter_index} is out of range "
                            f"at offset 0x{self.reader.offset:X}"
                        )

                    parameter_indices.append(parameter_index)

                data = {
                    "qname_index": qname_index,
                    "parameter_indices": parameter_indices,
                }

            else:
                raise ValueError(
                    f"Unknown multiname kind 0x{kind:02X} "
                    f"at offset 0x{kind_offset:X}"
                )

            self.model.pool.multinames.append(
                Multiname(
                    kind=kind,
                    data=data,
                )
            )

        return self




    def parse_traits(self):
        count = self.read_u30()
        traits = []

        for _ in range(count):
            name_index = self.read_u30()
            self._validate_pool_index(
                name_index,
                self.model.pool.multinames,
                "Multiname",
            )

            kind_attr = self.reader.read_u8()
            kind = kind_attr & 0x0F
            attributes = kind_attr >> 4

            trait = TraitInfo(
                name_index=name_index,
                kind=kind,
                attributes=attributes,
            )

            if kind in (0, 6):          # Slot / Const
                trait.slot_id = self.read_u30()
                trait.type_name = self.read_u30()
                trait.value_index = self.read_u30()

                if trait.value_index:
                    trait.value_kind = self.reader.read_u8()

            elif kind == 4:             # Class
                trait.slot_id = self.read_u30()
                trait.class_index = self.read_u30()

            elif kind == 5:             # Function
                trait.slot_id = self.read_u30()
                trait.function_index = self.read_u30()

            elif kind in (1, 2, 3):     # Method / Getter / Setter
                trait.disp_id = self.read_u30()
                trait.method_index = self.read_u30()

            else:
                raise ValueError(
                    f"Unknown trait kind: {kind}"
                )

            if attributes & 0x4:
                metadata_count = self.read_u30()
                trait.metadata = [
                    self.read_u30()
                    for _ in range(metadata_count)
                ]

            traits.append(trait)

        return traits





    def parse_method_bodies(self):
        count = self.read_u30()
        self.model.method_body_count = count

        for _ in range(count):
            method_index = self.read_u30()
            max_stack = self.read_u30()
            local_count = self.read_u30()
            init_scope_depth = self.read_u30()
            max_scope_depth = self.read_u30()

            code_length = self.read_u30()

            code = self.reader.read(code_length)

            exception_count = self.read_u30()
            exceptions = []

            for _ in range(exception_count):
                exceptions.append(
                    ExceptionInfo(
                        from_offset=self.read_u30(),
                        to_offset=self.read_u30(),
                        target_offset=self.read_u30(),
                        exception_type=self.read_u30(),
                        variable_name=self.read_u30(),
                    )
                )

            traits = self.parse_traits()

            body = MethodBodyInfo(
                method_index=method_index,
                max_stack=max_stack,
                local_count=local_count,
                init_scope_depth=init_scope_depth,
                max_scope_depth=max_scope_depth,
                code=code,
                exceptions=exceptions,
                traits=traits,
            )

            # Bytecode decoding remains opt-in until the opcode table is complete.
            self.model.method_bodies.append(body)

        return self

    def parse_scripts(self):
        count = self.read_u30()
        self.model.script_count = count

        for _ in range(count):
            init = self.read_u30()

            traits = self.parse_traits()

            self.model.scripts.append(
                ScriptInfo(
                    init=init,
                    trait_count=len(traits),
                    traits=traits,
                )
            )

        return self

    def parse_classes(self):
        # class_info[] has no separate count field.
        # It contains exactly instance_count entries.
        count = self.model.instance_count
        self.model.class_count = count

        for _ in range(count):
            cinit = self.read_u30()

            traits = self.parse_traits()

            self.model.classes.append(
                ClassInfo(
                    cinit=cinit,
                    trait_count=len(traits),
                    traits=traits,
                )
            )

        return self

    def parse_instances(self):
        count = self.read_u30()
        self.model.instance_count = count

        for _ in range(count):
            name_index = self.read_u30()
            super_name_index = self.read_u30()

            flags = self.reader.read_u8()

            protected_ns = 0
            if flags & 0x08:
                protected_ns = self.read_u30()

            interface_count = self.read_u30()
            interfaces = [
                self.read_u30()
                for _ in range(interface_count)
            ]

            iinit = self.read_u30()

            traits = self.parse_traits()

            self.model.instances.append(
                InstanceInfo(
                    name_index=name_index,
                    super_name_index=super_name_index,
                    flags=flags,
                    protected_ns=protected_ns,
                    interfaces=interfaces,
                    iinit=iinit,
                    trait_count=len(traits),
                    traits=traits,
                )
            )

        return self

    def parse_metadata(self):
        count = self.read_u30()
        self.model.metadata_count = count

        for _ in range(count):
            name_index = self.read_u30()

            self._validate_pool_index(
                name_index,
                self.model.pool.strings,
                "String",
            )

            item_count = self.read_u30()
            items = []

            for _ in range(item_count):
                key = self.read_u30()
                value = self.read_u30()

                self._validate_pool_index(
                    key,
                    self.model.pool.strings,
                    "String",
                )

                self._validate_pool_index(
                    value,
                    self.model.pool.strings,
                    "String",
                )

                items.append((key, value))

            self.model.metadata.append(
                MetadataInfo(
                    name_index=name_index,
                    items=items,
                )
            )

        return self

    def parse_methods(self):
        count = self.read_u30()
        self.model.method_count = count

        for _ in range(count):
            param_count = self.read_u30()
            return_type = self.read_u30()

            self._validate_pool_index(
                return_type,
                self.model.pool.multinames,
                "Multiname",
            )

            parameter_types = []

            for _ in range(param_count):
                type_index = self.read_u30()

                self._validate_pool_index(
                    type_index,
                    self.model.pool.multinames,
                    "Multiname",
                )

                parameter_types.append(type_index)

            name_index = self.read_u30()

            if name_index:
                self._validate_pool_index(
                    name_index,
                    self.model.pool.strings,
                    "String",
                )

            flags = self.reader.read_u8()

            optional_values = []
            parameter_names = []

            if flags & 0x08:
                option_count = self.read_u30()

                for _ in range(option_count):
                    value_index = self.read_u30()
                    value_kind = self.reader.read_u8()

                    optional_values.append(
                        (value_index, value_kind)
                    )

            if flags & 0x80:
                for _ in range(param_count):
                    parameter_names.append(
                        self.read_u30()
                    )

            self.model.methods.append(
                MethodInfo(
                    parameter_types=parameter_types,
                    return_type=return_type,
                    name_index=name_index,
                    flags=flags,
                    optional_values=optional_values,
                    parameter_names=parameter_names,
                )
            )

        return self
