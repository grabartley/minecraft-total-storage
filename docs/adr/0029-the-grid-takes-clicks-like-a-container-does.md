# ADR 0029: The grid takes clicks the way a container does, and the cursor decides which way they go

- **Status:** Accepted
- **Date:** 2026-09-07

## Context

Per ADR 0025 the terminal's item grid is not made of slots, so none of vanilla's click handling reaches it. Every button, modifier and cursor state over the grid has to be given a meaning explicitly, and the meanings chosen are what a player's hands will assume for the life of the mod.

The obvious reading of "clicking a stack in the inventory deposits it" is that a plain left click on a player inventory slot hands that stack to the network. That reading makes the player's own inventory unusable inside the terminal: there is then no gesture left for picking a stack up, splitting it, or moving it between the hotbar and the backpack, all of which players do constantly in front of a container.

## Decision

The grid follows the rule every vanilla container already follows: **a full cursor puts down and an empty cursor picks up.**

| Gesture | Over the grid | Over a player inventory slot |
|---|---|---|
| Left click, empty cursor | Take a stack onto the cursor, or everything in the cell if it holds less | Vanilla: pick the stack up |
| Right click, empty cursor | Take half of that, rounded up | Vanilla: pick half up |
| Shift and left click | Take a stack into the inventory, or everything in the cell if it holds less | **Deposit the whole stack** |
| Ctrl or Cmd and left click | Open the tag panel for that item, for a player who may edit tags | Vanilla |
| Middle click | Ask for a custom amount | Vanilla |
| Left click, full cursor | **Deposit the whole cursor stack** | Vanilla: put the stack down |
| Right click, full cursor | **Deposit one from the cursor** | Vanilla: put one down |

"A stack" is the item's own maximum stack size, so sixteen ender pearls or one pickaxe. The amount is worked out from the count the cell shows, so a cell of 48 logs gives 48 on a left click and 24 on a right click, the way vanilla gives half of what a slot holds. The server still clamps the request to what the network really holds. A gesture that moves everything the cell showed reports nothing. Only a request that falls short of what the cell showed, because the listing was stale or another player got there first, reports a shortfall.

Depositing over the grid ignores which cell the pointer is over, because a network has no slot to drop into.

Shift-click is the one mouse gesture reassigned. Every other mouse gesture on the player's own inventory behaves exactly as vanilla: picking up, splitting, and dragging. Moving a stack between the hotbar and the backpack is done by picking it up and putting it down, since shift-click now hands it to the network instead. Deposit all is a button, not a gesture.

The keyboard belongs to the search box, which takes focus as the terminal opens so that typing narrows the grid without a click first. A number key therefore writes a digit rather than swapping a hotbar slot, which is the trade the search box was given focus for.

## Consequences

A player who has used any container in the game already knows how to use the terminal, and the two halves of the screen do not contradict each other.

Shift-clicking is the gesture that carries the whole-stack deposit, which is the same gesture vanilla uses to push a stack into the container in front of you. That is why it is intercepted rather than left to `quickMove`: there is no slot on this screen for the stack to move into.

Accepted drawback: there is no single-click deposit from an inventory slot. The gesture is spent on picking the stack up, which is worth more, and shift-click plus deposit all cover the cases that matter. A player who wants to deposit part of a stack picks it up and right-clicks it onto the grid.

Second drawback: the custom amount prompt is a mode. While it is up the grid takes no clicks and the search box takes no keys. It closes on Escape, on confirming, and on any click, so it cannot be got stuck in.
