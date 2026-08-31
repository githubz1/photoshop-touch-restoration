from __future__ import annotations


class PostDominatorAnalysis:

    def compute(self, cfg):

        if not cfg.blocks:
            return {}

        exit_block = cfg.blocks[-1]

        post = {
            block: set(cfg.blocks)
            for block in cfg.blocks
        }

        post[exit_block] = {exit_block}

        changed = True

        while changed:

            changed = False

            for block in reversed(cfg.blocks[:-1]):

                if not block.successors:
                    continue

                new = set(post[block.successors[0]])

                for succ in block.successors[1:]:
                    new &= post[succ]

                new.add(block)

                if new != post[block]:
                    post[block] = new
                    changed = True

        return post
