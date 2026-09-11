# ADR 0020: The sort button wears vanilla chrome and bakes in its glyph

- **Status:** Accepted
- **Date:** 2026-08-29

## Context

Every other piece of art in this mod is drawn inside a screen the mod owns. The terminal screen sets its own chassis, its own recess, its own accent, and the sprites that fill it agree with each other because one hand drew all of them.

The sort button is not that. It is drawn into a chest screen, a hopper screen, a shulker box screen, a survival inventory, and whatever a third party mod happens to open, on top of chrome that was drawn by somebody else and cannot be changed. The terminal's cool steel chassis and redstone accent are a deliberate identity in the terminal. Dropped into a vanilla chest they read as a foreign object sitting on the panel.

The terminal's own toolbar buttons are composed: a `button/*` chassis sprite in one of three states, with a 12x12 `icon/*` drawn at a fixed offset on top. That composition is what lets one chassis serve seven different actions.

## Decision

**The sort button borrows vanilla's container palette, and each state ships as one complete sprite with the glyph already in it.**

The greys are sampled from `generic_54.png`: the `373737` slot border becomes the button outline, `C6C6C6` the face, `FFFFFF` and `555555` the bevel, `404040` the glyph. The button is a raised element made out of the same values as the panel it sits on, so it reads as a control that screen always had.

The glyph is baked in rather than composed. There is exactly one action here, so a chassis that can carry seven icons buys nothing, and two of the four states move the glyph or recolour it: held shifts it down and right by a pixel, disabled mutes it to `8B8B8B`. Composing those would need a matching set of icon variants and an offset that has to change with the state, which is more moving parts than drawing four sprites.

The reference table lives in [`../sort-button-art.md`](../sort-button-art.md).

## Consequences

The button reads as part of whatever screen it lands in, including modded screens, because it is made of the values every container screen already uses.

An implementation names a state and a rectangle and gets a finished button. There is no icon offset to keep in step with the state, so the held state's one pixel shift cannot drift out of alignment with the bevel that motivates it.

Accepted drawback: the mod now maintains two palettes. A change to the terminal palette does not reach the sort button and should not, but nothing enforces that and nothing catches someone reaching for the wrong one. The two are kept in separate documents, and each sprite source repeats its own palette, which is the only guard.

Accepted drawback: the glyph exists twice, as `icon/sort` in the terminal palette and inside the four sort button sprites in vanilla's. If the sorting metaphor changes, both have to change. Keeping them in step is a review concern, not something the build checks.

Accepted drawback: baking the glyph in means the button cannot be nine-sliced, so it is fixed at the one size drawn. That size is not free to choose: vanilla leaves a 14 pixel band between the panel bevel and the first slot row, so the button is 14x14 and a second size would be four more sprites rather than a different draw rectangle.

Accepted drawback: vanilla's greys are only correct for as long as vanilla uses them. A resource pack that restyles container screens leaves the button matching a panel that no longer exists, where the terminal palette would at least have been consistently foreign.
