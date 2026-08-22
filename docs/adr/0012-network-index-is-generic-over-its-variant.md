# ADR 0012: The network index is generic over its variant type

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

ADR 0001 makes `ItemVariant` the index key, and the engineering standards require that logic worth testing has no Minecraft dependency. Those two pull in opposite directions here, because `ItemVariant` cannot exist outside a running game.

Its class initialiser builds a codec against `Registries`, which throws `Not bootstrapped` in a plain JVM. `Bootstrap.initialize()` does not rescue it: the unit test source set has none of Loom's production access widening, so bootstrapping dies with an `IllegalAccessError` partway through block registration. It cannot be mocked either, since Mockito has to initialise the interface to instrument it.

So an index typed directly on `ItemVariant` would have had no unit tests at all. Every claim about aggregation, count arithmetic, and ordering stability would only ever have been checked by spinning up a server.

## Decision

`NetworkIndex`, `NetworkIndexEntry`, and `NetworkIndexBuilder` are generic over the variant type. They are used exclusively as `NetworkIndex<ItemVariant>` in production. The ordering takes a sort key function rather than reaching for the registry itself.

`NetworkIndexScan` and `ItemVariantSortKey` are the only classes that name `ItemVariant`. They hold no logic beyond reading storage views and looking up a registry identifier, and they are covered by gametests.

## Consequences

Aggregation, saturation, query behaviour, and ordering stability are unit tested against plain values, which is where the bugs that matter live: an inverted filter, a wrapped count, an unstable comparator that makes the grid reshuffle between openings. None of that needs a server, so the feedback loop is a second rather than a minute.

The seam is also where the reuse is. Sorting, filtering, and deposit routing all read the index, and none of them need a variant type to reason about totals and origins.

Accepted drawbacks: a type parameter that production never varies is a small ongoing "why is this generic" tax on every reader, which is what this record exists to answer. The split also means the game-touching half is proven only by gametests, so a mistake in reading storage views surfaces later in the loop than a mistake in aggregation does.
