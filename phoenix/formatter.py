"""
Pretty-printer for AVM2 constant-pool objects.
"""

from phoenix.abc_model import Multiname


class Formatter:

    def __init__(self, abc):
        self.abc = abc

    def string(self, index):
        if 0 <= index < len(self.abc.strings):
            return self.abc.strings[index]

        return f"<string:{index}>"

    def namespace(self, index):
        if index <= 0 or index >= len(self.abc.namespaces):
            return ""

        namespace = self.abc.namespaces[index]
        name_index = getattr(namespace, "name_index", 0)

        if 0 <= name_index < len(self.abc.strings):
            return self.abc.strings[name_index]

        return f"<ns:{index}>"

    def namespace_set(self, index):
        if index <= 0 or index >= len(self.abc.namespace_sets):
            return []

        namespace_set = self.abc.namespace_sets[index]

        indices = getattr(namespace_set, "namespace_indices", None)

        if indices is None:
            indices = getattr(namespace_set, "namespaces", None)

        if indices is None and isinstance(namespace_set, (list, tuple)):
            indices = namespace_set

        if indices is None:
            return [f"<ns-set:{index}>"]

        return [
            self.namespace(namespace_index)
            for namespace_index in indices
            if self.namespace(namespace_index)
        ]

    def qualified_name(self, namespace, name):
        if namespace:
            return f"{namespace}::{name}"

        return name

    def multiname_with_set(self, data):
        name = self.string(data["name_index"])
        namespaces = self.namespace_set(
            data["namespace_set_index"]
        )

        if not namespaces:
            return name

        if len(namespaces) == 1:
            return self.qualified_name(
                namespaces[0],
                name,
            )

        return (
            "{"
            + " | ".join(namespaces)
            + "}::"
            + name
        )

    def multiname(self, obj):
        if not isinstance(obj, Multiname):
            return str(obj)

        kind = obj.kind
        data = obj.data

        # QName / QNameA
        if kind in {0x07, 0x0D}:
            namespace = self.namespace(
                data["namespace_index"]
            )
            name = self.string(data["name_index"])
            return self.qualified_name(namespace, name)

        # Multiname / MultinameA
        if kind in {0x09, 0x0E}:
            return self.multiname_with_set(data)

        # RTQName / RTQNameA
        if kind in {0x0F, 0x10}:
            name = self.string(data["name_index"])
            return f"<runtime-namespace>::{name}"

        # RTQNameL / RTQNameLA
        if kind in {0x11, 0x12}:
            return "<runtime-namespace>::<runtime-name>"

        # MultinameL / MultinameLA
        if kind in {0x1B, 0x1C}:
            namespaces = self.namespace_set(
                data["namespace_set_index"]
            )

            if not namespaces:
                return "<runtime-name>"

            if len(namespaces) == 1:
                return (
                    namespaces[0]
                    + "::<runtime-name>"
                )

            return (
                "{"
                + " | ".join(namespaces)
                + "}::<runtime-name>"
            )

        # TypeName, such as Vector.<T>
        if kind == 0x1D:
            base_index = data["qname_index"]

            if 0 <= base_index < len(self.abc.multinames):
                base = self.multiname(
                    self.abc.multinames[base_index]
                )
            else:
                base = f"<multiname:{base_index}>"

            parameters = []

            for index in data["parameter_indices"]:
                if 0 <= index < len(self.abc.multinames):
                    parameters.append(
                        self.multiname(
                            self.abc.multinames[index]
                        )
                    )
                else:
                    parameters.append(
                        f"<multiname:{index}>"
                    )

            return (
                base
                + ".<"
                + ", ".join(parameters)
                + ">"
            )

        return f"<kind=0x{kind:02X}>"
