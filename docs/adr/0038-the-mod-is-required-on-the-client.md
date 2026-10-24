# ADR 0038: The mod is required on the client to join a server running it

- **Status:** Accepted
- **Date:** 2026-09-30
- **Supersedes:** the "vanilla clients can still sort" consequence of [ADR 0003](0003-server-side-sorting.md)

## Context

ADR 0003 put sorting on the server, and one of the reasons given was that a player with no mods installed could still sort a chest with `/ts sort`. The PRD made that player an actor, and release criterion REL-9 required an unmodified client to be tested against a modded server.

The 0.1.0 playtest ran that test. An unmodified 1.21.1 client joined a dedicated server running the mod and was disconnected about a second later, failing to decode the recipe list. The server sends every recipe on join, the terminal recipe names the terminal item, and a vanilla client has no item with that id.

The recipe list is only the first packet that names the mod's content. Chunks holding a terminal name its block state, and any inventory, container or dropped item holding a terminal names its item. Keeping a vanilla client connected means either sending it vanilla stand-ins for the terminal block and item, which is what server-side content libraries do, or keeping every packet that names the mod's content away from it, which only works if a vanilla client can never meet a terminal.

## Decision

Vanilla clients are not supported. A player needs the mod installed to join a server running it.

The server says so instead of letting the client fail on a packet. During the configuration phase, before any recipe or chunk is sent, it checks whether the client registered the mod's configuration channel. A client that did not is disconnected with a message that names the mod and tells the player to install it. The message carries its English text as the fallback, so a client without the mod's language file still shows it.

The check uses the channels the client registered, not anything the client says about itself.

## Consequences

A player without the mod sees a clear reason on the disconnect screen instead of a decode error.

`/ts sort` and `/ts quickstack` stay. They are still how a player sorts without the button or the keybind, and how an operator reaches the same logic from the console.

Accepted drawback: the mod cannot be put on a public server for players who will not install anything. Serving them would mean either a new dependency on a server-side content library or a set of packet rewrites for every place the terminal can appear, both of which are more to own than this promise was worth for 0.1.0.

Accepted drawback: other Fabric clients that lack this mod are refused too, even ones that would never have met a terminal.
