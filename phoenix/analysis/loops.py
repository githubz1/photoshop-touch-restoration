from __future__ import annotations


class LoopDetector:

    """
    Detects natural loops by finding CFG back edges.

    This is the first stage of loop recovery.
    """

    def find_back_edges(self, cfg):
        back_edges = []

        for block in cfg.blocks:
            for successor in getattr(block, "successors", ()):
                if successor.id <= block.id:
                    back_edges.append(
                        (block, successor)
                    )

        return back_edges
