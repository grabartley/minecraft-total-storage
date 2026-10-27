# ADR 0042: Fuzzy search is a per-player choice, off by default

- **Status:** Accepted
- **Date:** 2026-10-27
- **Supersedes:** "anything else is matched fuzzily" in [ADR 0013](0013-search-terms-combine-by-kind.md)

## Context

ADR 0013 matched every name term fuzzily, so any item whose name held the typed letters in order was a result. Fuzzy matches were ranked last (ADR 0028), but they were still in the grid.

On a large network a short term finds a lot of items that share its letters by chance. A player who types `iron` and sees items that merely hold an i, r, o and n in that order reads the grid as broken, not as clever. Some players like fuzzy search and some do not, and it is a matter of taste, not of server policy.

## Decision

Fuzzy matching of name terms is a per-player choice, off by default. With it off, a name term matches the display name or the identifier path exactly, as a prefix or as a substring. With it on, it also matches letters in order, ranked after every other kind of match, as before.

The choice is stored on the player on the server, so it follows them to every terminal and every machine, survives death, and is synced only to them. A small toggle at the end of the search box shows it and changes it. Changing it re-runs the open search at once.

Everything else in ADR 0013 stands: tag and mod terms never match fuzzily, a term with a namespace is matched literally, and the namespace is never matched by a bare term.

## Consequences

A new player sees only results that contain what they typed, which is what most search boxes do. Players who want `dpick` to find a Diamond Pickaxe turn it on once.

The choice is not a server setting, so an operator cannot force it either way. It is not a client file either, so it does not carry between servers.

The toggle takes width from the search box.
