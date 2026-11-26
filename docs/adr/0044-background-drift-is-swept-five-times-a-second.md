# ADR 0044: Background drift is swept five times a second, and a region is rescanned once a tick

- **Status:** Accepted
- **Date:** 2026-11-26
- **Amends:** the tick sweep in [ADR 0027](0027-the-listing-is-narrowed-on-the-server.md) and the per-chunk rescan in [ADR 0039](0039-a-placed-container-joins-on-the-next-tick.md)

## Context

[ADR 0027](0027-the-listing-is-narrowed-on-the-server.md) pushed a fresh page to every viewer of a network at the end of any tick in which its index owed a recompute. [ADR 0032](0032-a-commit-resyncs-everyone-who-can-see-it.md) then moved every change this mod makes onto the commit, so the sweep only answers writers that are not this mod: hoppers, furnaces, droppers, other mods.

Those writers change something almost every tick. A hopper chain feeding a chest left the network stale on most ticks, so every viewer got a rebuilt listing, settings panel and tags panel twenty times a second. The player cannot read a grid that changes that often, and the server paid for every one.

A chunk arriving in a network's region triggered a full rescan of the region. Walking into a large network loads many chunks in the same tick, and each one started its own full rescan. Every arrival is delivered at the end of the tick that loaded it, so the first rescan already sees every chunk that arrived with it.

## Decision

**The sweep runs every fourth tick.** A network that owes a recompute on a due tick sends its viewers a fresh page then. A change by this mod still resyncs at commit, in the same tick, as ADR 0032 decided. Only drift from outside waits, and never more than three ticks.

**A network rescans its region at most once a server tick.** The first chunk or container arrival in a tick rescans. Later arrivals in the same tick are skipped, because the scan they would run sees the same world. Power on, an explicit rescan and a radius change are not limited.

## Consequences

A hopper filling a chest under an open terminal costs a fifth of what it did. The listing still moves while the player watches, five times a second instead of twenty.

A burst of chunk loads costs one scan rather than one per chunk.

The rescan limit relies on arrivals being delivered from the end of tick queue, after the chunks they describe have loaded. A caller that changed the world and announced an arrival twice in one tick would see its second announcement skipped. Nothing in the mod does that.
