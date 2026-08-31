from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Region:
    kind: str
    header: object
    condition: object | None = None
    blocks: set = field(default_factory=set)
    loop_type: str = "unknown"


class RegionBuilder:

    def build(self, cfg, loops):

        regions = []

        for header, blocks in loops.items():

            regions.append(
                Region(
                    kind="loop",
                    header=header,
                    blocks=set(blocks),
                )
            )

        for region in regions:

            header = region.header

            if header.predecessors:

                outside = [
                    pred
                    for pred in header.predecessors
                    if pred not in region.blocks
                ]

                if outside:
                    region.loop_type = "while"
                else:
                    region.loop_type = "do-while"

        return regions


    def find_if_regions(self, cfg, dom, loops=None):

        regions = []
        loop_headers = set(loops.keys()) if loops else set()

        for block in cfg.blocks:

            if block in loop_headers:
                continue

            last = block.instructions[-1]

            if not last.mnemonic.startswith("if"):
                continue

            if len(block.successors) != 2:
                continue

            body = {
                candidate
                for candidate in cfg.blocks
                if block in dom[candidate]
            }

            regions.append(
                Region(
                    kind="if",
                    header=block,
                    blocks=body,
                )
            )

        return regions
