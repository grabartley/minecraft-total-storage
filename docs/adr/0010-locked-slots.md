# ADR 0010: Locked slots are slot-based and survive death

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

Deposit-all empties a player's inventory into the network in one click, which is the mod's core convenience. It is also alarming for the many players who keep tools, weapons, and food in fixed positions and navigate by muscle memory. Sorting the inventory has the same problem: it tidies, and it destroys the layout the player built deliberately.

Without a way to pin items in place, those players cannot use the mod's two best features.

## Decision

Players may lock inventory slots. Locks are **slot-based**, not item-based: slot three is locked regardless of what occupies it.

Locks block **automated** movement only, meaning sorting, deposit-all, quick stack, and future loadout swaps. Manual dragging, shift-clicking, and hotbar swapping are unaffected.

Locks survive death. The items drop normally per vanilla rules; the slot stays locked and is simply empty afterwards.

Enforcement is server-side inside each operation, never a client-side filter.

## Consequences

Slot-based locking matches the actual requirement, which is preserving a layout rather than protecting particular items. A player who uses a pickaxe until it breaks and puts a new one in the same slot keeps their arrangement without re-locking anything.

Locks not blocking manual movement is the distinction that makes the feature usable. A lock that fought the player's own hands would be worse than no lock, and players would stop locking things.

Surviving death while the items still drop keeps two mechanics separate. The lock preserves layout; it is not a keep-inventory mechanic, and conflating them would make it a balance change rather than a convenience.

Fabric's Data Attachment API supplies all three requirements directly through `persistent`, `copyOnDeath`, and `syncWith`, so persistence and respawn behaviour need no custom handling.

Accepted drawback: every automated path must honour locks, and each new automated feature inherits that obligation. A path that forgets is worse than the feature not existing, because the player has stopped checking. This is covered by a test per path rather than by care.

Locked slots also become the mechanism that makes loadout presets safe later, since a preset swap deposits what the player carries and locked slots are exactly the things that should survive it.
