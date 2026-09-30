# ADR 0039: A container placed in range joins the network on the next tick

- **Status:** Accepted
- **Date:** 2026-09-30
- **Supersedes:** the "a chest placed in range is not picked up until its chunk reloads" drawback of [ADR 0014](0014-containers-leave-on-sight-and-return-on-chunk-load.md)

## Context

ADR 0014 accepted that a chest placed in range of a powered terminal would not join the network until its chunk next loaded. It justified this with "a terminal that is re-scanned on opening does not feel the gap". Opening the terminal never re-scanned, so the gap was real: a playtest placed four chests next to a working network, reopened the terminal, and still saw six containers instead of ten until Rescan was pressed. A player growing a storage room will not know to press Rescan.

The drawback existed because the two obvious fixes both looked wrong. Scanning on every block entity load sounded too expensive, and admitting one block entity at a time would skip the slot identity check that makes the two halves of a double chest one container.

## Decision

A block entity loading inside a chunk a powered network's region touches is treated like a chunk load: the network re-scans its region on the next tick and reconciles the result against what it already holds.

- The callback records only which chunk something arrived in. The scan runs on the next tick, as ADR 0026 requires, and arrivals in the same chunk in the same tick are one scan.
- If a chunk load for the same chunk is already queued, the block entity load adds nothing, and a chunk load cancels a block entity load queued for its chunk.
- Nothing is queued at all while no network is powered.
- The scan is the normal discovery scan, so a new container goes through the same identity check as any other, and a double chest stays one container.
- The network remembers each container's slot count from the last scan. A container that is still in the network but whose slot count changed, which is what happens when a single chest becomes a double chest, has its contents read again. Every other container keeps what was already read, so a placement does not re-read a network that did not change.

## Consequences

A chest, barrel or shulker box placed in range is in the network one tick later. A deposit made after that reaches it.

An idle network does no extra work. The cost is one region scan per chunk per tick in which a block entity appeared, which happens when a player places something, not on any tick of its own.

Accepted drawback: placing any block entity in range, a sign or a furnace included, costs a scan. Checking whether the new block is a container would mean reading the world inside the callback, which ADR 0026 forbids, and the scan is the check.

Accepted drawback: breaking one half of a double chest still drops the pair until something else triggers a scan, for the reason ADR 0014 gives.
