# ADR 0011: Preset membership is computed, and Miscellaneous is derived

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

[ADR 0006](0006-tags-scoped-per-network.md) settled that preset tags ship with the mod and are not editable. It did not settle how an item finds its way into one, and three questions were still open.

First, whether an item belongs to exactly one preset or several. A single preset gives a clean mental model and a cheap index, but it forces an arbitrary choice for oak planks, which are equally Wood and Building Blocks.

Second, where the preset lists live. A Java list of items is the obvious first move and is immediately readable, but it goes stale the moment the game adds an item, and it cannot see modded content at all.

Third, what happens to an item no preset claims. A player who cannot find an item in the filter column reasonably concludes the mod has lost it.

## Decision

An item holds every preset it matches, not one.

Each preset is backed by a mod-owned item tag under `too-many-chests:preset/`, composed in data out of vanilla tags, convention (`#c:`) tags, and a curated item list only where neither has a suitable tag. Membership is read live from the tag bindings on every lookup rather than snapshotted at load.

`Miscellaneous` has no tag. It is what the resolver returns when nothing else matched, and it never appears alongside a real preset.

Food is the one preset that also consults a data component. Any item carrying a food component is filed under Food whether or not a tag lists it.

## Consequences

Composing in data is what keeps the presets current. A vanilla item added in a future version arrives already tagged, and a modded item that follows the convention tags lands in a sensible preset with no code change. A pack can extend or override a preset the same way it would any other tag, exactly as it can already for the `too-many-chests:network_ineligible` block tag.

Reading membership live means a datapack reload reclassifies items immediately, with no migration and no stale index to invalidate.

Deriving `Miscellaneous` rather than storing it means it can never disagree with the other presets. There is no state to repair when a pack adds a tag that claims an item previously filed as miscellaneous.

The food component check exists because tags describe what modders remembered to declare and components describe what the item actually is. For edibility the component is the more reliable signal, and it is the one case where the payoff justifies the special case.

Accepted drawbacks:

- An item appearing under several presets means the filter column's counts overlap, so they cannot be summed to a total. The alternative was arbitrary single-preset assignment, which is worse.
- Membership is recomputed per lookup. This is cheap today and stays cheap as long as callers resolve per item rather than per stack, but it is a real cost the network index will need to cache once the index exists.
- Around 7% of vanilla items still fall to `Miscellaneous`, almost all of them spawn eggs and creative-only technical blocks. This is treated as correct rather than as a coverage gap. Presets exist to organise the items players store, and a preset per creative-menu curiosity would make the filter column worse, not better.
