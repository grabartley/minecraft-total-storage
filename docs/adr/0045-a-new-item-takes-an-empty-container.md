# ADR 0045: A new item takes an empty container before one holding other items

- **Status:** Accepted
- **Date:** 2026-11-26

## Context

Deposit routing ranked containers by whether they held the item, then by distance. Every other `General` container shared one rung. An item the network had never held went into the nearest chest with room, even when that chest was full of something unrelated and an empty one sat a block further away. Over time the network ended up with odd items scattered through chests that were otherwise tidy, which is the mess the player built the network to avoid.

## Decision

**Within each role, a container holding the item comes first, then an empty container, then a container holding other items.** `Filtered` containers still come before `General` ones, as before. Distance and then position break ties inside a rung.

**What a container holds is updated as the deposit runs.** Deposit all with two new items puts the first in an empty chest, and that chest then counts as holding the first item and as not empty for the second. The second item takes another empty chest if there is one.

Consolidation uses the same order for the `Filtered` containers it may move items into.

## Consequences

A new kind of item starts its own chest while empty chests remain, and later deposits of it top that chest up.

A network with no empty chests behaves as before: the nearest chest with room takes the item.

An empty chest is preferred even when a nearer chest has free slots. A player who wants new items kept close should keep an empty chest close to the terminal.
