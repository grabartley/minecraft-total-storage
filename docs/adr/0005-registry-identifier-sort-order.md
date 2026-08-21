# ADR 0005: Sort ordering uses registry identifiers, not display names

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

Sorting needs an ordering rule. Alphabetical by display name is the obvious choice and matches what most sorting mods do. It reads naturally, and players can predict where an item will land.

Display names are localised. The same item sorts differently in English and in Portuguese.

## Decision

The default ordering is: category following the preset tag grouping, then registry identifier, then data components, then stack count descending. Display-name ordering is available as an explicit configuration option.

The comparator is a pure function over item variants and counts, shared by the terminal grid, in-container sorting, and consolidation.

## Consequences

Storage does not rearrange itself when a player changes their game language. Under display-name ordering it would, which is a genuinely confusing bug: the player changes an unrelated setting and their base reorganises. On a server with players using different languages, it is worse, because sorting is a server operation and whichever player sorted last decides the order for everyone.

Registry identifiers group items from the same mod together, which is usually what a player wants in a modded setting.

The ordering is stable and testable without a running game, and sorting an already-sorted container provably produces zero moves. That last property matters more than it sounds: an unstable comparator causes needless transactions and resyncs every time anyone sorts.

Accepted drawback: identifier order occasionally surprises. `minecraft:oak_planks` and `minecraft:spruce_planks` sit together, which reads well, but items whose identifier differs from their display name land somewhere players do not expect. The category grouping mitigates most of this, and players who prefer name order can switch.
