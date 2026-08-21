# ADR 0009: Clients send intent, servers resolve it

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

The terminal shows an aggregated list of everything in the network. A large network holds more distinct item variants than fit comfortably in a packet, so the client receives a paginated window rather than the whole list, and that window is stale the moment it arrives.

The convenient implementation has the client tell the server what it clicked: this container, this slot. The server extracts from there. It is simple and it is exactly how a storage mod gets emptied by a modified client, because a server that extracts from a client-supplied position is a server that can be told to extract from anywhere.

## Decision

Requests carry **what the player wants**, never **where it is**. A withdrawal names an item variant and an amount. It carries no container position, no slot index, and no page offset.

The server resolves where the item actually lives, against live containers, at the moment of extraction, inside a transaction. Any positional information a client holds is a display hint that is revalidated, never trusted.

The client's list is a display cache. It never decides what a withdrawal is allowed to take.

## Consequences

A modified client gains nothing by lying about positions, because positions are not part of the protocol.

Staleness stops being a correctness problem and becomes an ordinary race with a defined outcome. A request for more than exists grants what exists and reports the shortfall. A request for something already gone grants nothing and resyncs the client. Neither is an error, and neither can fabricate items.

Search and filtering must be evaluated server-side, because the client does not hold the full list to filter. This follows directly from pagination and is not extra cost.

Permission is naturally re-checked per operation, since every operation already reaches the server for resolution.

Accepted drawback: every operation costs a server-side lookup rather than a direct indexed access. On a network large enough for this to matter, the index makes the lookup cheap, and correctness here is worth far more than the microseconds saved.
