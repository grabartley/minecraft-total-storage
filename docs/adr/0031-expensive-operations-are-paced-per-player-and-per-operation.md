# ADR 0031: Expensive operations are paced per player and per operation

- **Status:** Accepted
- **Date:** 2026-09-08

## Context

Most of what the mod does is cheap. A withdrawal touches one variant across a handful of containers, and the network index answers a listing without walking the world at all.

Two gestures are not cheap, and both are a single click:

- **Rescan** walks the terminal's whole region block by block. That is what it is for, and it is the only way a network recovers from drifting away from the world under it.
- **Deposit all** routes every occupied slot of an inventory at once, which reads every container in the network to decide where each variant lands.

A player holding one of those buttons down is issuing a packet per click, and the server does the whole walk each time. Nothing in the protocol stops that: per [ADR 0009](0009-clients-send-intent.md) the request carries intent, and intent that is legitimate the first time is legitimate the tenth. Sorting and consolidation will join this list when they arrive, for the same reason.

The obvious answer, dropping the extra requests silently, is the wrong one. A button that does nothing is indistinguishable from a mod that has stopped working, and that is the first reading a player reaches for.

## Decision

Expensive operations are paced by a per-player, per-operation cooldown, and a refused request says so.

**Per operation, not shared.** A player who has just emptied their inventory has not thereby spent their right to rescan. A single counter would make one button appear to break the other, which is a worse bug than the one being fixed.

**Per player, not global.** One player's pace is not another's, and a shared limit would let one person rate limit a whole server.

**Refusals speak.** The guard knows how long is left, so it is the guard that talks. A refusal for pace is worded differently from a refusal for permission: one is about how fast you asked and the other about whose terminal it is, and a player who reads the wrong one goes looking for the wrong problem.

**Nothing half-happens.** A refused operation is refused before it looks at a container. A rate limit that moved some items would cost a player their stuff in exchange for protecting the server, which is not a trade the mod is entitled to make.

The arithmetic lives in `OperationCooldowns`, which holds no clock and reads no config; the tick and the cooldown are handed in. `OperationGuards` supplies both and turns a refusal into a line of text. That split is what lets every boundary be proven without a running server, which matters because a cooldown one tick out eats every other legitimate click and is invisible in anything but an exact test.

## Consequences

The cooldown is `expensiveOperationCooldownTicks`, ten ticks by default. `0` switches it off, which is what the floor of the setting means; a server that would rather not pace its players can say so, and one that has been abused can raise it.

Cooldowns are held in memory and forgotten when a player disconnects. They are not a security boundary and do not survive a restart, which is correct: the thing being protected is a tick budget, and a restart has already spent it.

A player who genuinely wants to rescan twice waits half a second. A player trying to spend the server's tick budget gets one walk per cooldown however fast they click.

Because the cooldown is server-wide state, gametests that spend one reset it per batch and any test that changes the setting runs in a batch of its own. Batches run in parallel and share the JVM, so a test that set the value in one would otherwise decide the outcome of a test in another.
