# ADR 0026: A world callback defers its scan to the next tick

- **Status:** Accepted
- **Date:** 2026-09-06

## Context

ADR 0014 decided that a network picks containers up when a chunk it reaches finishes loading, and ADR 0007 decided that a terminal starts its network when it loads holding a powered block state. Both of those moments are announced by Fabric from inside the chunk load itself: `CHUNK_LOAD` and `BLOCK_ENTITY_LOAD` are delivered while the server is part way through loading that chunk.

Both moments answer with a discovery scan, and a discovery scan asks the chunk manager for every chunk column the terminal's region touches. On the server thread `ServerChunkManager.getChunk` pumps main-thread tasks and joins the chunk's future. Asked from inside a chunk load, that is re-entrant, and for a chunk still on its way to full status the join never returns.

The result was a world holding a powered terminal that could not be loaded at all: the client sat on `Preparing spawn area: 0%` and a dedicated server hung at the same step. The scan was already correct about ADR 0002 and never asked for a chunk to be created; the problem was where it was being asked from, not what it asked for.

## Decision

Nothing that reads the world runs inside a world callback. A callback announcing an arrival records what arrived and the work happens on the next end of server tick, by which time every world has finished ticking and no chunk is part way through loading.

The queue is keyed, so the same chunk or the same terminal announced repeatedly before the tick is worth one piece of work. Deferred work re-reads the world when it runs rather than trusting what was true when it was queued, so a terminal that has been broken or whose chunk has gone again is answered with nothing.

Departures are not deferred. Dropping a container from a network reads nothing at all, and ADR 0014 depends on a departure being immediate. That freedom is conditional on the departure genuinely reading nothing: a departure that looks a terminal up asks the world for a block entity, which loads that terminal's chunk back in, and a removal announced while chunks are unloading then unloads and reloads the same chunk forever. Anything answering a departure reads only what is already loaded.

## Consequences

The entire class of re-entrant chunk-manager hang is gone, and no future caller can reintroduce it by adding another read to a load callback, because load callbacks no longer do reads.

A terminal is inert for at most one tick after its chunk arrives, and a container placed in range is picked up one tick after its chunk loads rather than during it. Neither is observable at 20 ticks a second.

A terminal placed into an already powered position also starts one tick later, since placement and chunk load are the same event as far as Fabric is concerned. Treating them alike is deliberate: a callback cannot reliably tell which of the two it is answering, and a rule with an exception is a rule that gets it wrong.

Accepted drawback: the queue is one more piece of process state that has to be cleared when a server stops, alongside the network registry and the lifecycle registry.
