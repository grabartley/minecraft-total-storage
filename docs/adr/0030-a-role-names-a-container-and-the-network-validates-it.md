# ADR 0030: A role names a container, and the network validates it

- **Status:** Accepted
- **Date:** 2026-09-08

## Context

[ADR 0009](0009-clients-send-intent.md) is the rule the mod's safety rests on: a client sends what the player wants, never where it is. Every request for an item names a variant and an amount, and the server resolves the position itself.

Container roles cannot work that way. A role is a decision *about a particular container*, and there is nothing else to name it by. A chest has no identity that survives being broken, which is why [ADR 0024](0024-a-role-dies-with-the-container-it-was-given-to.md) keys assignments by position in the first place. A request to exclude "the third chest in the list" would be worse, not better: it would depend on an ordering the client and server compute separately, and a role would land on the wrong chest the moment the two disagreed.

The role edit is therefore the only request in the mod that carries a `BlockPos`, and it looks at first glance like an exception to the rule.

## Decision

The role edit names the container it is about. The server refuses any container the terminal's live network does not already reach.

That check is what keeps ADR 0009 intact, because the two rules are guarding different things:

| | ADR 0009 | This record |
|---|---|---|
| What a position would decide | which container items come out of | which container a decision is recorded about |
| What a forged position wins | items from anywhere in the world | nothing; the terminal refuses it |
| What the server does with it | never trusts one; resolves its own | validates it against the live network |

Whether the caller may make the decision at all is a separate question, answered by `TerminalCaller` against [ADR 0008](0008-terminals-owner-locked-by-default.md), and answered identically whether the request arrived from a screen or from a command line.

## Consequences

A forged role edit gains nothing. The worst it can do is name a container the sender's own terminal already reaches, which is a container they could have clicked anyway.

Assignments cannot accumulate for containers a network has never seen, so a save cannot be grown by a client sending positions at random. That is a real risk with position-keyed storage and it is the validation, not the permission check, that closes it.

Accepted drawback: reading "no request carries a position" as a blanket rule is now wrong, and someone will have to read this record to find out why. The alternative was an indirection through list indices that would have been less safe and much easier to get subtly wrong.

Second drawback: the check costs a lookup of the live network on every role edit. Roles are edited by hand, one click at a time, so this is not a cost that scales with anything.
