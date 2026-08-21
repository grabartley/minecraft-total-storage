# ADR 0007: The terminal is redstone gated

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

A terminal indexes every container within its radius. On a large base that is a lot of block entities, and the naive implementation rescans on a timer.

Rescanning on a tick timer is among the most common reasons storage and utility mods get removed from modpacks. A mod that costs measurable server performance while doing nothing is a mod pack authors drop, regardless of how good its features are.

The terminal also needed to feel like a piece of equipment rather than a magic box, and Minecraft already has a vocabulary for powered machinery.

## Decision

The terminal requires a redstone signal to operate. An unpowered terminal is completely inert: no index, no listeners, no scanning, no per-tick work. Powering on triggers exactly one discovery scan, and powering off tears down the index and unregisters every listener.

Settings persist in the block entity regardless of power state. Cutting power is an off switch, not a factory reset.

Index maintenance is event driven, hooked to container mutation, with recomputation deferred until something actually reads the index.

## Consequences

The mod has a clear, honest performance story for pack authors: a terminal you are not using costs nothing, and there is a switch to prove it. Combined with event-driven invalidation and a server-enforced radius cap, an idle base with several terminals costs nothing measurable.

Redstone gating is a natural Minecraft idiom, so the requirement reads as a design choice rather than a limitation, and it gives players a legitimate reason to run redstone to their storage room.

Power state also gives a clean answer to problems that would otherwise need invention: a terminal is trivially disabled, and the block state carries a visible lit variant so an inert terminal is diagnosable at a glance.

Accepted drawbacks: a player who forgets the power requirement has a terminal that does nothing, so the unpowered state must be visually obvious and the failed interaction must say why. Power loss while a player has the interface open needs defined behaviour, which is to complete the in-flight transaction and then close the screen, so items are never dropped mid-move.
