# ADR 0016: Locks are toggled by a modifier click, not a lock mode

- **Status:** Accepted. The tint as the main sign of a lock, "nothing is added to the screen" and the undiscoverable-gesture drawback are superseded by [ADR 0041](0041-a-lock-shows-as-a-badge-and-alt-shows-what-can-be-locked.md): the badge is the sign, the tint is off by default, and holding Alt shows what can be locked. The gesture itself stands unchanged.
- **Date:** 2026-08-28

## Context

Locking a slot needs a gesture, and the inventory screen has almost none left. Vanilla already owns left click, right click, shift click, click-drag split, double-click gather, hotbar number swap, and middle click in creative.

The obvious answer to a crowded screen is a mode: a button that puts the screen into lock editing, where clicks toggle locks instead of moving items. It is discoverable and it makes a mis-click harmless. It also adds a button to every screen showing the player inventory, adds a state the player can forget they are in, and answers a question the player only asks while they are already looking at the slot they want to lock.

## Decision

There is no lock mode. **Alt with the left mouse button toggles the lock on the slot under the cursor**, in any screen showing the player inventory.

Alt is the one modifier the inventory screen leaves free, so nothing is taken away from vanilla. The gesture is swallowed whether or not it lands on a lockable slot, because alt-clicking a chest slot or empty space is a miss, and a miss must not move a stack on the very screens where the player asked for things to stay put.

Locked slots are painted with a tint **behind** the item. That keeps the stack, its count, and its durability bar exactly as readable as they were, and it makes the lock visible at the moment and place the player is deciding about it.

The client sends a slot index and no desired state. The server owns the set, and the client's mirror is whatever the attachment last synced.

## Consequences

The state a player has to remember is the lock itself, which is drawn on the slot, rather than a mode they are in. Nothing is added to the screen, so no screen has to make room for it and no screen needs its own placement rule.

Sending an index rather than a state means a stale mirror cannot fight the server. Two toggles racing resolve to whatever the server applied in order, and both clients are then told the same answer.

Accepted drawback: a modifier gesture is undiscoverable on its own. Nothing on the screen suggests alt-click, so this has to be taught by documentation and by the mod's own configuration surface rather than by the inventory screen. That is the cost of not putting a button on every screen, and it is paid once per player rather than every time they open a chest.

Accepted drawback: a colour behind the item distinguishes locked slots by colour alone, which is not enough on its own for accessibility. A badge that reads without colour is owed on top of this tint, not instead of it.

Accepted drawback: the gesture is a client mixin ahead of vanilla's click handling, so a modified client can decline to use it. That client gains nothing. It can only send toggles the server would have validated anyway, and the moves it makes instead are moves vanilla already allows.
