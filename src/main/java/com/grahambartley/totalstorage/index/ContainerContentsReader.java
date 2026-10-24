package com.grahambartley.totalstorage.index;

import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.World;

public final class ContainerContentsReader
    implements Function<StorageOrigin, Map<ItemVariant, Long>> {
  private final World world;
  private final StorageProviderResolver resolver;

  public ContainerContentsReader(World world) {
    this(world, new StorageProviderResolver());
  }

  public ContainerContentsReader(World world, StorageProviderResolver resolver) {
    this.world = Objects.requireNonNull(world, "world");
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  @Override
  public Map<ItemVariant, Long> apply(StorageOrigin origin) {
    if (!(origin instanceof BlockStorageOrigin blockOrigin)
        || !blockOrigin.worldKey().equals(world.getRegistryKey())) {
      return Map.of();
    }
    return resolver
        .resolve(world, blockOrigin.pos())
        .map(ContainerContentsReader::contentsOf)
        .orElseGet(Map::of);
  }

  private static Map<ItemVariant, Long> contentsOf(StorageProvider provider) {
    final Map<ItemVariant, Long> held = new LinkedHashMap<>();
    for (final StorageView<ItemVariant> view : provider.storage().nonEmptyViews()) {
      held.merge(view.getResource(), view.getAmount(), NetworkIndexCounts::saturatedAdd);
    }
    return held;
  }
}
