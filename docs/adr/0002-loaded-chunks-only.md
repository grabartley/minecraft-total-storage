# ADR 0002: The network reflects loaded chunks only

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

A storage terminal lists the contents of nearby containers. Those containers sit in chunks that may unload while the world runs.

Two options existed. Cache container contents server-side, so the terminal can list items in unloaded chunks. Or restrict the network to loaded chunks, so the terminal only ever reports what it can currently see.

Caching sounds better. It is also how storage mods duplicate items: any cache that can go stale is a cache that can hand out items that no longer exist. Breaking a chest while its contents are cached, or editing the world while the server is down, both produce a terminal that confidently offers items that are not there.

## Decision

The network only ever reflects loaded chunks. No container contents are cached across an unload. Scanning never force-loads a chunk.

Because the terminal has a bounded radius and the player must stand at it to use it, the containers in range are loaded whenever the terminal is in use. The restriction costs almost nothing in practice.

## Consequences

The mod has no stale-cache duplication vector, which removes an entire category of bug from a mod whose reputation depends on not creating items.

Correctness becomes much easier to reason about: the index is a view over live block entities, so the only question is whether it is current, never whether it is true.

Chunk unload and reload need explicit handling. Items disappear from the listing when their chunk unloads and return when it loads, which must not be mistaken for item loss, and is worth documenting for players.

Accepted drawback: no remote access to storage from across the world. This mod is deliberately not Applied Energistics. If remote access is ever wanted, it needs a different design and a fresh decision record, not a cache bolted onto this one.
