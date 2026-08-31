from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class StructuredRegion:
    kind: str
    header: object
    blocks: set = field(default_factory=set)
    children: list["StructuredRegion"] = field(default_factory=list)


class StructuredRegionBuilder:

    def build(self, loops, if_regions):

        regions = []

        for header, blocks in loops.items():
            regions.append(
                StructuredRegion(
                    kind="loop",
                    header=header,
                    blocks=set(blocks),
                )
            )

        for header, blocks in if_regions.items():
            regions.append(
                StructuredRegion(
                    kind="if",
                    header=header,
                    blocks=set(blocks),
                )
            )

        return regions
