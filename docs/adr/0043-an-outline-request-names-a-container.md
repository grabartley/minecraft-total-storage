# ADR 0043: An outline request names a container

- **Status:** Accepted
- **Date:** 2026-10-30

## Context

[ADR 0009](0009-clients-send-intent.md) says a client sends what the player wants, never where it is. [ADR 0030](0030-a-role-names-a-container-and-the-network-validates-it.md) allows an exception for a decision about one container, because nothing else can name it. Role, name, and filter edits all rely on it.

A player can now click a row in the Containers tab to see which block it is. That request is about one container too, so it is a fourth case of the same exception.

## Decision

**An outline request carries a container position, and the server treats it as a claim to check.** The server confirms the position is in the live network, reads the role itself, and sends the outline only to the player who asked. A position outside the network is refused and the list refreshes.

Outlining changes nothing, so it needs only the right to open the terminal, not the right to administer it.

## Consequences

A modified client can ask about any position, but the reply only confirms what the list already shows that player.

The outline shows through blocks, because a chest behind a wall is the case this exists for. That means it must be drawn after block entities and translucent blocks, so it has its own render layer and its own render stage.
