# ADR 0041: A lock shows as a badge, and Alt shows what can be locked

- **Status:** Accepted
- **Date:** 2026-10-25
- **Supersedes:** the tint, "nothing is added to the screen" and the undiscoverable-gesture drawback in [ADR 0016](0016-locks-are-toggled-by-a-modifier-click.md)

## Context

ADR 0016 made a tint behind the item the main sign of a lock, with a badge added on top for players who cannot tell the tint by colour. Locks were also drawn on the in-game hotbar.

In play, the tint was the loudest thing on every screen that shows the player inventory. Three locked hotbar slots looked like an error, and they looked that way all the time, including on the hotbar during normal play, where nothing can be done about a lock.

ADR 0016 also accepted that Alt + click is undiscoverable, because nothing on the screen suggested it.

## Decision

**The badge is the sign of a lock. The tint is an option, off by default.**

The badge is a small two-tone padlock in the top-left corner of the slot, smaller than the badge it replaces. Two tones keep it readable on any item without relying on colour. The tint stays as a per-player display option for players who want it.

**Locks are drawn only in screens that show the player inventory**, where the player can change them. Nothing is drawn on the in-game hotbar.

**Holding Alt shows what can be locked.** While Alt is held, every slot that can be locked gets a faint ring, and a locked item's tooltip adds one line saying it is locked and how to unlock it. Without Alt, a locked item's tooltip is the vanilla tooltip. The Alt + click gesture itself is unchanged.

## Consequences

Screens with locked slots look like vanilla screens with a small mark in a corner.

The gesture now teaches itself: a player who holds Alt sees which slots respond to it. This pays down the drawback ADR 0016 accepted.

Accepted drawback: a player loses the at-a-glance view of locks on the in-game hotbar, including before a sort or quick stack run by keybind or command with no screen open. The hotbar view is dropped because it was constant noise during normal play, and any inventory screen shows and changes the locks.

The ring and the tint are drawn over slot pixels the mod does not own, so they fall under the slot tint exemption in the PRD's interface rules. The badge is a shipped sprite, so it stays inside the palette like every other sprite.
