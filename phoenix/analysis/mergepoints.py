from __future__ import annotations


class MergePointAnalysis:

    def immediate_post_dominators(self, post):

        ipost = {}

        for block, post_dominators in post.items():

            candidates = post_dominators - {block}

            if not candidates:
                ipost[block] = None
                continue

            parent = None

            for candidate in candidates:

                is_immediate = True

                for other in candidates:
                    if other is candidate:
                        continue

                    if candidate in post[other]:
                        is_immediate = False
                        break

                if is_immediate:
                    parent = candidate
                    break

            ipost[block] = parent

        return ipost

    def conditional_merge_points(self, cfg, ipost):

        merges = {}

        for block in cfg.blocks:

            if len(block.successors) != 2:
                continue

            if not block.instructions:
                continue

            last = block.instructions[-1]

            if not last.mnemonic.startswith("if"):
                continue

            merges[block] = ipost.get(block)

        return merges
