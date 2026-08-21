# ADR 0001: Storage providers are built on the Fabric Transfer API

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

Too Many Chests needs to read from and write to many kinds of container. The obvious approach is to handle each container type directly: special-case chests, then barrels, then shulker boxes, then discover that double chests are two block entities behind one screen, then discover that every other storage mod needs its own case too.

The mod also needs transactional item movement, since concurrent multiplayer access without atomicity duplicates items.

Fabric API already ships `fabric-transfer-api-v1`, which provides `ItemStorage.SIDED`, a `BlockApiLookup` returning a `Storage<ItemVariant>` for a position. It registers a fallback that wraps any block entity implementing `Inventory` or `SidedInventory`, and it includes a nested transaction system with rollback.

## Decision

All storage access goes through a provider abstraction built on `ItemStorage.SIDED`. The mod writes no per-container-type handling.

All item movement runs inside a `Transaction`, using `openOuter` and `openNested`, with `SnapshotParticipant` for the mod's own mutable state. `ItemVariant` is the index key.

## Consequences

Vanilla chests, barrels, and placed shulker boxes work with no specific code. Double chests arrive pre-combined as a single `CombinedSlottedStorage`, so the two-block-entities problem never surfaces. **Containers added by other mods work automatically**, which would otherwise be an endless compatibility burden and is a significant part of the mod's value in a modpack.

`ItemVariant` compares items along with their data components, so two swords with different enchantments stay distinct while identical stacks merge, with no bespoke comparison logic.

The transaction system is battle-tested by the wider Fabric ecosystem, which matters enormously for a mod whose worst failure mode is duplicating items.

Accepted drawbacks: a hard dependency on a specific Fabric API module, and contributors must learn the Transfer API's transaction semantics, which are unusual. Anything holding a `Storage` reference across ticks must revalidate it, because the underlying block entity may be gone. Counts are `long` rather than `int` throughout.

The abstraction deliberately avoids assuming a provider has a world position, so backpacks, which travel in a player's inventory, can implement it later without a rewrite.
