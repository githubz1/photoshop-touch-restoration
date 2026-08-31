from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(eq=False)
class RegionNode:
    region: object
    parent: "RegionNode | None" = None
    children: list["RegionNode"] = field(default_factory=list)


class RegionTreeBuilder:

    def build(self, regions):

        nodes = [
            RegionNode(region=r)
            for r in regions
        ]

        for node in nodes:

            containing = []

            for other in nodes:

                if node is other:
                    continue

                if not hasattr(node.region, "blocks"):
                    continue

                if not hasattr(other.region, "blocks"):
                    continue

                if (
                    node.region.blocks
                    and
                    node.region.blocks < other.region.blocks
                ):
                    containing.append(other)

            if containing:

                parent = min(
                    containing,
                    key=lambda n: len(n.region.blocks),
                )

                node.parent = parent
                parent.children.append(node)

        return nodes
