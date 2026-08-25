package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.World;

/**
 * Reads what one container currently holds, resolving its storage afresh every time.
 *
 * <p>Re-resolving rather than holding a {@code Storage} across ticks is what keeps the cache
 * honest: a container that has been broken or unloaded reads as empty on the next recompute instead
 * of reporting whatever it held when it was discovered.
 */
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
