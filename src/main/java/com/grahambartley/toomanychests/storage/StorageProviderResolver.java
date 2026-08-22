package com.grahambartley.toomanychests.storage;

import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Turns a world position into a {@link StorageProvider}, or nothing. */
public final class StorageProviderResolver {
  private final ItemStorageLookup lookup;

  public StorageProviderResolver() {
    this(SidedItemStorageLookup.INSTANCE);
  }

  public StorageProviderResolver(ItemStorageLookup lookup) {
    this.lookup = Objects.requireNonNull(lookup, "lookup");
  }

  public Optional<StorageProvider> resolve(World world, BlockPos pos) {
    return resolve(world, pos, ContainerRole.DEFAULT);
  }

  public Optional<StorageProvider> resolve(World world, BlockPos pos, ContainerRole role) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(pos, "pos");
    Objects.requireNonNull(role, "role");
    Storage<ItemVariant> storage = lookup.find(world, pos);
    if (storage == null) {
      return Optional.empty();
    }
    return Optional.of(new StorageProvider(BlockStorageOrigin.of(world, pos), storage, role));
  }

  /**
   * Re-resolves a provider held from an earlier tick, keeping its role. Empty when the container is
   * gone, or when the origin is not a world position this resolver can reach.
   */
  public Optional<StorageProvider> revalidate(World world, StorageProvider provider) {
    Objects.requireNonNull(provider, "provider");
    if (!(provider.origin() instanceof BlockStorageOrigin blockOrigin)) {
      return Optional.empty();
    }
    if (!blockOrigin.worldKey().equals(world.getRegistryKey())) {
      return Optional.empty();
    }
    return resolve(world, blockOrigin.pos(), provider.role());
  }
}
