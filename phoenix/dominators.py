from __future__ import annotations


class DominatorTree:

    def compute(self, cfg):

        if not cfg.blocks:
            return {}

        entry = cfg.blocks[0]

        dom = {
            block: set(cfg.blocks)
            for block in cfg.blocks
        }

        dom[entry] = {entry}

        changed = True

        while changed:

            changed = False

            for block in cfg.blocks[1:]:

                if not block.predecessors:
                    continue

                new = set(dom[block.predecessors[0]])

                for pred in block.predecessors[1:]:
                    new &= dom[pred]

                new.add(block)

                if new != dom[block]:
                    dom[block] = new
                    changed = True

        return dom


    def immediate(self, dom):

        idom = {}

        for block, dominators in dom.items():

            if len(dominators) == 1:
                idom[block] = None
                continue

            candidates = dominators - {block}

            parent = None

            for candidate in candidates:

                dominated = False

                for other in candidates:
                    if other is candidate:
                        continue

                    if candidate in dom[other]:
                        dominated = True
                        break

                if not dominated:
                    parent = candidate
                    break

            idom[block] = parent

        return idom


    def find_loops(self, cfg, dom):

        loops = []

        for block in cfg.blocks:

            for successor in block.successors:

                if successor in dom[block]:
                    loops.append(
                        (
                            successor,
                            block,
                        )
                    )

        return loops


    def build_natural_loops(self, cfg, dom):

        loops = {}

        for header, backedge in self.find_loops(cfg, dom):

            body = loops.setdefault(header, {header})

            stack = [backedge]

            while stack:

                block = stack.pop()

                if block in body:
                    continue

                body.add(block)

                for predecessor in block.predecessors:
                    stack.append(predecessor)

        return loops


    def compute_post(self, cfg):

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
