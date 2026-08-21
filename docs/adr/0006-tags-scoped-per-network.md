# ADR 0006: Tags are scoped per network

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

Tags let players index storage by meaning rather than by chest, and an item can carry several at once. Their scope had to be decided: per network on the terminal, per player following them around the server, or server-wide.

An argument for per-player scope: "cobblestone is a building block" is a fact about cobblestone, not about a particular base, so re-tagging at every terminal is repetitive.

An argument for per-network scope: a tag set describes how *this* storage area is organised, which is genuinely local knowledge. A shared base benefits from everyone seeing the same categories, and tags sit naturally alongside radius, roles, and access settings, which are unambiguously per network.

## Decision

Tags are scoped per network and stored in the terminal's block entity. Preset tags ship with the mod, derived from vanilla item tags, and are not editable. Custom tags are created on the terminal.

To offset the repetition, a tag set can be copied from another terminal the player owns, and exported and imported through operator commands.

## Consequences

Tags sit with the rest of the network's configuration, so there is one place to look and one thing to back up. A shared base has a shared vocabulary rather than each player seeing their own private categories over the same chests.

Preset tags mean a freshly placed terminal has a populated filter column with no setup, which matters because a mod that requires configuration before it is useful loses most players at the first screen. Deriving them from vanilla item tags keeps that coverage current as the game adds items, and convention tags extend it to modded items.

Accepted drawback: a player with several bases configures tags more than once. Copy, export, and import reduce this to a couple of clicks rather than eliminating it.

Tags attach to item identifiers rather than full item variants, so an enchanted diamond sword inherits the tags of a diamond sword. Component-level tagging was considered and rejected as complexity without a matching payoff.
