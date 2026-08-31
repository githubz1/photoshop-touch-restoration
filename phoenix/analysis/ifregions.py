from __future__ import annotations

from collections import deque


class IfRegionRecovery:

    def recover(self, cfg, merge_points):

        regions = {}

        for header, merge in merge_points.items():

            body = set()

            if merge is None:
                regions[header] = body
                continue

            for successor in header.successors:

                if successor is merge:
                    continue

                queue = deque([successor])

                while queue:

                    block = queue.popleft()

                    if block is merge:
                        continue

                    if block in body:
                        continue

                    body.add(block)

                    for nxt in block.successors:
                        if nxt not in body and nxt is not merge:
                            queue.append(nxt)

            regions[header] = body

        return regions
