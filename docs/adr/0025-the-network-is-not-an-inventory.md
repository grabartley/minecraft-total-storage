# ADR 0025: The terminal's screen handler holds no network slots

- **Status:** Accepted
- **Date:** 2026-09-06

## Context

Every vanilla container screen is a `ScreenHandler` over an `Inventory`. Modelling the storage network the same way is the obvious move, and it is what the whole shape of the API invites: add a slot per stack, let vanilla sync them, get shift-clicking and drag-splitting for free.

A `ScreenHandler` addresses its contents by index and syncs them one stack at a time, over a sync id that is a single byte. A network reaching a modest base already holds more distinct item variants than that can address, and the mod's entire premise is that it will hold far more. Any slot-based design therefore has to decide, on opening, which fraction of the network the player is allowed to see, and re-sync the whole set whenever they page.

## Decision

The terminal's screen handler carries the player's own inventory and nothing else. The network is not exposed as slots, is not an `Inventory`, and no request names a slot index in it.

The screen opening payload carries only the terminal's position, its effective reach, the player's permission for that terminal, and the network's tag set. Its size is fixed by the terminal's configuration rather than by what the network holds.

The player's inventory **is** modelled with ordinary slots, because deposits move real stacks out of it and vanilla's slot handling is exactly right for that half.

## Consequences

Opening a terminal costs the same packet whether it reaches one empty barrel or a warehouse, which is what makes the block usable on a shared server at all.

Every network operation has to be its own request, resolved server-side against live containers, which is already what ADR 0009 requires for safety. The two decisions reinforce each other: nothing addresses a network position, so nothing can lie about one.

Accepted drawback: none of vanilla's slot behaviour is inherited for the network half. Withdrawing, depositing, shift-clicking into the network and drag-splitting across it are all written by hand, and each one has to be tested rather than trusted. The alternative was a listing with a hard ceiling on how much of a base it could show, which is not a storage mod.

A second drawback: because the listing is not slot state, vanilla will not resync it. The terminal keeps its own record of who is looking, and a committed change resyncs them explicitly. A viewer entry that outlives its screen is therefore a leak with consequences, and every way a screen can end has to clear it.
