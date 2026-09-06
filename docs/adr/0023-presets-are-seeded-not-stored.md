# ADR 0023: Presets are seeded into a tag set, not stored in it

- **Status:** Accepted
- **Date:** 2026-09-06

## Context

[ADR 0006](0006-tags-scoped-per-network.md) put a network's tag set on the terminal's block entity, and [ADR 0011](0011-preset-membership-is-computed.md) settled that a preset's item membership is computed from item tags rather than listed anywhere. What was still open is whether the presets themselves are part of the saved tag set.

Writing them out is the obvious move: the set the terminal holds is then exactly the set that was saved, with no assembly on read. It also means a save is a complete description of the network, which is a pleasant property for a backup.

The cost of writing them out is that a save then pins the preset list to the build that wrote it. A player who configured a terminal before a preset existed never sees the new one. A player whose save is hand-edited can delete a preset the mod guarantees is always there, and every reader downstream has to cope with a filter column that is missing a category the code assumed.

## Decision

Only custom tags are persisted and only custom tags travel to the client. Presets are seeded into every tag set on construction, from the compiled-in `PresetTag` enum, wherever that set comes from: a save, a packet, or a fresh terminal.

A preset owns its id. A stored tag whose id collides with a preset is dropped rather than allowed to shadow it.

## Consequences

Every tag set contains every preset, as an invariant of the type rather than as something each reader has to check. The filter column cannot be missing a category, whatever a save says, and a preset cannot be renamed or deleted by editing a file.

A build that adds a preset adds it to networks configured before it existed, with no migration. This is the same property [ADR 0011](0011-preset-membership-is-computed.md) bought for membership, applied one level up to the list of presets itself.

Saves are smaller and describe what the player actually authored, which makes a hand-edited tag file readable.

Accepted drawbacks:

- A save is no longer a complete description of a network's vocabulary. Restoring it onto a build with a different preset list gives a different set of tags, by design.
- A future preset that takes an id a player already used for a custom tag swallows that tag and its assignments. Dropping the custom tag is the safe direction, because the alternative is a network where a preset silently means something the mod did not define, but it is a real loss and the change would need calling out in release notes.
