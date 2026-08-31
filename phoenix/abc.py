from __future__ import annotations

from dataclasses import dataclass

from phoenix.abc_parser import ABCParser


@dataclass
class ABCFile:
    minor: int
    major: int

    ints: list[int]
    uints: list[int]
    doubles: list[float]
    strings: list[str]

    namespaces: list
    namespace_sets: list
    multinames: list

    namespace_count: int
    namespace_set_count: int
    multiname_count: int
    method_count: int

    methods: list
    metadata: list
    instances: list
    classes: list
    scripts: list
    method_bodies: list


def parse(data: bytes) -> ABCFile:
    """
    Parse an AVM2 ABC file.

    At the moment this parses the header and constant pools.
    The remaining ABC sections will be migrated into ABCParser
    incrementally.
    """

    parser = ABCParser(data)

    (
        parser
        .parse_header()
        .parse_integer_pool()
        .parse_uint_pool()
        .parse_double_pool()
        .parse_string_pool()
        .parse_namespace_pool()
        .parse_namespace_sets()
        .parse_multiname_pool()
        .parse_methods()
        .parse_metadata()
        .parse_instances()
        .parse_classes()
        .parse_scripts()
        .parse_method_bodies()
    )

    return ABCFile(
        minor=parser.minor,
        major=parser.major,
        ints=parser.pool.ints,
        uints=parser.pool.uints,
        doubles=parser.pool.doubles,
        strings=parser.pool.strings,
        namespaces=parser.pool.namespaces,
        namespace_sets=parser.pool.namespace_sets,
        multinames=parser.pool.multinames,
        namespace_count=parser.namespace_count,
        namespace_set_count=parser.namespace_set_count,
        multiname_count=parser.multiname_count,
        method_count=parser.model.method_count,
        methods=parser.model.methods,
        metadata=parser.model.metadata,
        instances=parser.model.instances,
        classes=parser.model.classes,
        scripts=parser.model.scripts,
        method_bodies=parser.model.method_bodies,
    )
