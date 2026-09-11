# ADR 0017: Terminal GUI art ships as atlas sprites, not a sprite sheet

- **Status:** Accepted
- **Date:** 2026-08-28

## Context

The terminal screen is the surface players spend the most time in front of. It needs a chassis, a recessed screen area, a search field, a filter column, a dense item grid, a scrollbar, section tabs, a toolbar of buttons, and two empty states, each with resting, hovered, active and disabled variants where the interaction calls for them.

The traditional way to ship that is one sprite sheet PNG plus a table of pixel offsets, which is how vanilla shipped container backgrounds for most of the game's life and how the art brief for this work was written.

A sheet couples the art to the code twice over. Every draw call carries a `u`, `v`, a width and a height, so moving one sprite inside the sheet silently shifts every widget that reads past it, and nothing catches it until someone looks at a screenshot. Worse for this screen specifically: a sheet region is a fixed rectangle, and almost every piece of terminal chrome has to survive being drawn at a size nobody knows in advance. The filter column widens with the longest tag name. The scrollbar thumb's height is a fraction of the content. The search field spans whatever is left after the toolbar. Stretching a fixed region distorts its border; tiling it by hand is a loop in every widget.

Since 1.20.2 the game stitches `textures/gui/sprites/` into a GUI atlas on its own, addresses each sprite by identifier, and reads a per-sprite `.mcmeta` that says whether to stretch, tile, or nine-slice it when the draw size differs from the texture size.

## Decision

**Terminal GUI art ships as individual sprites under `assets/too-many-chests/textures/gui/sprites/`, with nine-slice metadata on everything whose drawn size is not known in advance.**

Nothing in the screen code names a pixel offset. A widget names a sprite and a rectangle, and the border comes out the right width whatever that rectangle is.

The sprites are generated from palette-mapped text sources under `art/gui/`, and both the source and the PNG are committed. A text source reviews as a diff that shows which pixels moved; a PNG reviews as "1 file changed".

Every state pair differs by shape and not only by colour: a bevel that appears, a bevel that inverts, a bevel that is removed. Accent detailing is placed so that it lands in a nine-slice band that tiles along the axis it runs, so a bar stays a bar at any height. Which colours those sprites are allowed to use was settled later, by [ADR 0035](0035-the-terminal-wears-vanillas-chrome.md).

The reference table lives in [`../terminal-gui-sprites.md`](../terminal-gui-sprites.md), which is what the screen implementation reads.

## Consequences

The screen implementation cannot get a sprite's geometry wrong, because it never states one. Adding a sprite is adding a file, and it cannot disturb any sprite that already exists.

Chrome adapts to content instead of content being cut to fit chrome. The filter column can be any width, the thumb any height, and the search field any span, with no per-widget stretching code and no clipped grid.

Because the sources are text, an art change and a code change review the same way, and a reviewer can see that a hover state gained an outline without opening an image editor.

Accepted drawback: sprite lookups go through the atlas by identifier, so a typo in an identifier fails at draw time with a missing-texture square rather than at compile time. The reference table exists to keep those strings in one place, but it is a document, not a compiler.

Accepted drawback: the PNGs are generated, so the repository holds the same art twice. The two can drift if someone edits a PNG directly. The regeneration command is one line and is documented next to the sources, but nothing enforces it.

Accepted drawback: nine-slice needs a border small enough to leave a centre, which puts a floor under how small a piece of chrome can be drawn. The scrollbar thumb cannot go below six pixels tall without its own borders overlapping.

Accepted drawback: a per-sprite `.mcmeta` is hand written and can disagree with the texture it sits beside. A border larger than half the texture leaves no centre and the sprite does not draw at all, which is loud, but a border that is merely wrong is quiet and only shows up when the sprite is stretched far from its native size.
