# ADR 0014: Containers leave the network on sight, and return only on a chunk load

- **Status:** Accepted
- **Date:** 2026-08-25

## Context

ADR 0002 decided that the network reflects loaded chunks only, which leaves the question of how a network finds out that one of its containers has gone. A container never announces its own removal: breaking a chest marks nothing dirty, and unloading a chunk does not touch the block entities in it one by one.

The discovery scan that builds a network is the most expensive operation in the mod, so re-running it whenever the world changes is not an option. Doing nothing is worse: a network holding a container that no longer exists is exactly the stale provider that duplicates items.

## Decision

Departures and arrivals are handled differently, and deliberately so.

A container leaves the moment the world says it has. Fabric's block entity unload event covers breaking and replacing, its chunk unload event covers the rest, and both drop the container from the network immediately, without a scan and without reading anything.

A container joins only when a chunk the network reaches finishes loading, and only then does a re-scan run. The scan is reconciled against the current membership rather than replacing it, so the containers that never went anywhere keep the contents already read for them.

Nothing is ever force-loaded to keep a container reachable.

## Consequences

The expensive operation runs when a chunk loads, which is rare and already an expensive moment, rather than whenever anything at all changes.

Being wrong is only ever possible in the safe direction. The window between a container going and the network hearing about it does not exist, while the window between a container appearing and the network noticing may last until its chunk next loads. A network can therefore under-report, and cannot over-report, which is the only asymmetry that matters for a mod that must not create items.

Accepted drawback: breaking one half of a double chest drops the pair from the network, because the two halves were collapsed into one container when they were discovered. The surviving half returns when the chunk next loads. Re-scanning on removal would fix it, and would also re-admit the container that is being removed, since the block entity is still present while the event is being delivered.

Accepted drawback: a chest placed in range of a powered terminal is not picked up until its chunk reloads. Picking it up would mean either scanning on every block entity load, or admitting single block entities without the slot-identity check that collapses double chests. Neither is worth it here, and a terminal that is re-scanned on opening does not feel the gap.
