# ADR 0003: Sorting runs on the server

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

The mod adds a sort button to container screens. Several established mods do this client-side, so the approach clearly works, and client-side sorting has the real advantage of functioning on vanilla servers.

Four existing mods were compared:

| Mod | Side | Mechanism |
|---|---|---|
| Simple Sorting | Client only | Queues simulated vanilla mouse clicks |
| Client Sort | Client, optional server | Click replay, plus a server companion for near-instant sorting |
| Inventory Sorting | Server, optional client | Works with vanilla clients; the client mod adds buttons and keybinds |
| Inventory Profiles Next | Client | Click packets, using hotbar and crafting grid as scratch space |

Client-side sorting works by replaying dozens of vanilla slot-click packets, which the server validates normally. That is what makes it safe on vanilla servers, and it is also its ceiling.

## Decision

Sorting runs on the server. The client sends one sort-intent packet naming the screen handler it has open. The server validates, sorts inside a transaction, and resyncs every viewer. The client mod contributes the button and keybind only.

## Consequences

Sorting is atomic. Click replay cannot be: sorting a double chest is over fifty sequential packets, so a disconnect part way leaves a half-sorted chest, and two players in one chest can interleave clicks. Client Sort's own documentation describes the vanilla path as rate limited, and ships duplication-detection policies to guard its accelerated server path, which is a direct signal about where the sharp edges are.

Locked slots become possible. Inventory Profiles Next documents that skipping locked slots requires a server-side component.

Cross-container consolidation becomes possible at all. Slots cannot be clicked in a container that is not open, so a client-side implementation could never gather items across chests.

**Vanilla clients can still sort**, through a command, because the logic lives server-side. A player joins with no mods installed and sorting works, which is a genuine advantage for modpacks and public servers.

Accepted drawback: the mod must be installed on the server. This was already true for every other feature, so it costs nothing here. The main argument for client-side sorting, compatibility with servers that do not have the mod, does not apply to a mod that is server-installed regardless.
