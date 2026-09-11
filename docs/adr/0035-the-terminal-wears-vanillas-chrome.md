# ADR 0035: The terminal wears vanilla's chrome rather than its own

- **Status:** Accepted
- **Date:** 2026-09-11

## Context

The terminal screen shipped with a palette of its own: a cool steel chassis around `ACACB8`, a deep blue-black screen recess, and a redstone accent, chosen so the screen would read as a machine rather than as a chest. The grid under it was on a 20 pixel pitch with the item inset two pixels, which gave every cell a gutter to draw its state in.

Both decisions were defensible in isolation and wrong together with everything else on the screen. A player opens the terminal from the same hotbar, with the same inventory drawn underneath it, as every other container in the game. The player's own inventory is drawn by vanilla, on vanilla's chassis, in vanilla's greys, immediately below a panel in a palette nothing else in Minecraft uses. The seam is the first thing anyone sees.

The pitch made it worse in a way that is easy to mistake for a rendering bug. The item itself is 16x16 in both, because `drawItem` does not scale. What differs is everything around it: a 20 pixel cell puts two pixels of air on each side of the item where a chest puts one, so nine columns of terminal grid are 18 pixels wider than the nine columns of player inventory directly beneath them, and the two never line up. The report that arrived was "items don't render at the same size as in a vanilla interface", which is exactly what a mismatched pitch looks like from the outside.

## Decision

**Every surface the mod draws uses Minecraft's own container chrome, and the mod's grid uses Minecraft's own slot geometry.**

Concretely:

- The chassis, the recessed surfaces, the bevels and the slot frames are copied pixel for pixel out of `gui/container/generic_54.png` and the creative inventory's textures, not designed to resemble them.
- A grid cell is a vanilla slot: 18 pixels, item at `+1,+1`. `SLOT_SIZE` is one constant behind the grid and the player inventory both, so the columns line up by construction rather than by arithmetic that happens to agree.
- A hovered cell is highlighted by calling `HandledScreen.drawSlotHighlight`, the same call vanilla makes.
- Item overlays are drawn by `DrawContext.drawItemInSlot`, so durability bars and cooldown sweeps appear exactly as they do in a chest.
- Text uses vanilla's label grey, and secondary text reuses a chrome colour rather than introducing a new one.

Two colours survive from the old palette, and only two: the redstone accent appears on a filter that is currently filtering and on a container the network may not write to. Both are places where one row differs from its neighbours and that difference is the whole message, and neither relies on the colour alone: an accent bar is a shape, and the role is written out in words beside it.

## Consequences

The window is narrower and shorter, because a 162 pixel grid needs less room than a 180 pixel one. That is spare screen, not lost function: the grid still holds forty-five cells and the settings sections still hold four rows each, now at the slot pitch.

The sprite set shrank from thirty-two files to twenty. Naming a sprite for the chrome it draws rather than the widget that draws it collapsed a recessed row, a recessed scrollbar channel and a slot into one sprite, and deleted the bespoke hover and selection cells entirely in favour of vanilla's own highlight.

"Looks vanilla" stopped being a matter of opinion. `VanillaPaletteTest` reads `generic_54.png` off the test classpath and fails if a palette constant drifts from the pixel vanilla paints there. `TerminalSpriteTest` nine-slices the shipped sprites using the border their own `.mcmeta` declares and compares the result to Minecraft's texture pixel for pixel, and fails if any sprite paints a colour outside the palette. A future change that quietly reintroduces a bespoke grey fails the build rather than a code review.

The cost is that the terminal no longer announces itself as a machine. That was a real idea, and this trades it for the screen reading as part of the game. The accent is what is left of it, and it now marks state instead of identity.

## Alternatives Considered

**Keep the palette, fix only the pitch.** This fixes the reported symptom and leaves the cause. The grid would line up with an inventory drawn in a different colour scheme, which reads as a bug rather than as a style.

**Keep the 20 pixel cell and centre the inventory under it.** The columns still do not correspond, and the grid still reads looser than every other container in the game. The gutter it bought was only ever needed because the cell state was drawn as a bespoke sprite; vanilla's highlight needs no gutter.

**Ship a resource pack that restyles the mod.** This puts the default on the wrong side. The mod should look native out of the box, and a player who wants the machine look can pack over it, which resource packs already allow for every sprite here.
