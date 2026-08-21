# ADR 0008: Terminals are owner-locked by default

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

A terminal is a single interface that can withdraw from every container in its radius. That is the entire point of the mod, and it is also a serious hazard on a shared server: without access control, any player who walks up to any terminal can drain an entire base through one convenient screen, far faster than opening chests by hand.

Vanilla Minecraft has no container permission system, so players on public servers rely on distance, obscurity, or a protection mod. A storage terminal defeats all three at once.

## Decision

Every terminal has an access mode: **Owner** (default), **Trusted** (owner plus a trust list), or **Open**. The owner is recorded by UUID on placement. Operators can always open any terminal for moderation, and that access is logged. Server configuration sets the default mode for newly placed terminals.

Permission is checked server-side on **every operation**, not only when the screen opens.

## Consequences

The mod is shippable on public servers without a protection mod, which materially widens where it can be used.

Checking per operation rather than per open closes the window where a player keeps a screen open while their access is revoked and continues withdrawing. It costs a check on a path that is already doing far more expensive work.

Recording the owner by UUID rather than by name means ownership survives a name change, and offline players resolve correctly in trust lists.

The operator bypass is a deliberate hole. A server administrator must be able to inspect and fix a player's storage, and pretending otherwise just means administrators use more destructive tools instead. Logging the access is the mitigation.

Accepted drawback: single player and small friendly servers get a permission system they did not ask for. The server-wide default setting lets those servers switch to Open once, rather than every player changing every terminal.
