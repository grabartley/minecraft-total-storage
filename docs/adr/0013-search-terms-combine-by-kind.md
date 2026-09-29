# ADR 0013: Search terms combine by kind

- **Status:** Accepted, refined in part by [ADR 0028](0028-results-are-ranked-before-they-are-paged.md)
- **Date:** 2026-08-22

> ADR 0028 keeps the rules below for what a query accepts and changes only who orders what it
> accepted: the server, between narrowing and paging, rather than the terminal grid. Acceptance is
> now read from the same match strength the ordering uses, so this record's "the evaluator answers
> only whether a row belongs" holds as behaviour while the sentences below about the grid ordering
> the results, and about the evaluator being reusable without a notion of relevance, are answered
> there instead.

## Context

The search box accepts three kinds of term in one string: bare text for names, `#tag`, and `@modid`. A query can hold several of each, so how repeated terms combine had to be decided before the evaluator could exist.

The obvious rule is that every term narrows, because that is what search boxes do everywhere else and it is what a player expects when they keep typing. It is also wrong for one of the three: an item comes from exactly one mod, so requiring `@create` and `@minecraft` at once can only ever return nothing. A rule that quietly makes a query useless is worse than an inconsistent one.

The tag filter column has its own AND or OR mode, which raised a second question: whether typing into the search box should be narrowed by the column, or should search past it.

## Decision

Name terms and tag terms combine with AND, so each one narrows. Mod identifier terms combine with OR, so each one widens.

Search always combines with the filter column using AND, whatever mode the column itself is in. The column's mode governs only how the selected tags combine with each other.

A bare term containing a namespace separator is matched literally against the full item identifier. Anything else is matched fuzzily against the display name and against the identifier's path, never against the namespace.

A `#tag` term and an `@modid` term name an identifier, so they match a tag id or a mod id, or one of its words between underscores and dashes, that equals the term or starts with it, and nothing looser. Substring and fuzzy matching belong to name terms only.

The evaluator answers only whether a row belongs in the results. Ordering the results it accepts is the terminal grid's job.

## Consequences

Every query a player can type means something. Typing more of one kind of term does what that kind of term is for, and no combination silently collapses to an empty grid for a reason the player cannot see.

Excluding the namespace from fuzzy matching is what keeps `mine` from matching every vanilla item at once, which it otherwise would through the shared `minecraft:` prefix that every identifier carries. Players who know an identifier can still type it out, and get an exact match rather than a fuzzy one.

Matching identifiers by the start of a word is what keeps `#stone` from finding everything tagged `redstone`, and keeps a short `@` term from finding any mod whose id happens to hold those letters in order. `#base` and `#kit` both find `base-kit`, and `#armour` finds `weapons_and_armour`, because a player names a multi-word tag by whichever word they remember. A `#tag` search that names one tag in full agrees with that tag's count in the filter column, unless another tag's id starts with the same word.

Keeping acceptance separate from ranking means the sorting epic and the deposit routing epic can reuse the evaluator without inheriting a notion of relevance that only the terminal grid cares about.

Accepted drawbacks: the AND and OR split between term kinds is an asymmetry a player will not discover on their own, and nothing in the interface announces it. Two mod identifier terms is also a rare enough query that the rule mostly exists to prevent a confusing result rather than to enable a useful one.
