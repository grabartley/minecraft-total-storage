# ADR 0047: Locks are drawn only in the terminal

- **Status:** Accepted
- **Date:** 2026-11-26
- **Supersedes:** "Locks are drawn only in screens that show the player inventory" in [ADR 0041](0041-a-lock-shows-as-a-badge-and-alt-shows-what-can-be-locked.md)

## Context

ADR 0041 drew the lock badge, and the tint for players who turn it on, in every screen that shows the player inventory. That puts a mark on the player inventory, every chest, every shulker box and every modded container a player opens. Most of the time the player is not thinking about locks in those screens, so the marks are visual noise.

## Decision

**The badge and the tint are drawn only in the terminal.** Every other screen looks like vanilla.

**Alt still works everywhere.** In any screen that shows the player inventory, Alt + click still toggles a lock, holding Alt still rings every lockable slot that is not locked, and a locked item's tooltip still adds its line while Alt is held.

## Consequences

Vanilla screens carry no mark from the mod unless the player holds Alt.

Accepted drawback: outside the terminal a player cannot see at a glance which slots are locked. Holding Alt shows it indirectly, since a locked slot is the one without a ring, and the tooltip names it. A player who sorts or quick stacks from the inventory has to hold Alt or open a terminal to check what will be skipped.

The tint option now affects only the terminal.
