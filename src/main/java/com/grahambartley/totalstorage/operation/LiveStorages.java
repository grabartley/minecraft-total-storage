package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

public final class LiveStorages {
  private final StorageProviderResolver resolver;

  public LiveStorages() {
    this(new StorageProviderResolver());
  }

  public LiveStorages(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  public List<Storage<ItemVariant>> of(
      ServerWorld world, Collection<? extends StorageOrigin> origins) {
    Objects.requireNonNull(world, "world");
    if (origins == null || origins.isEmpty()) {
      return List.of();
    }
    final List<Storage<ItemVariant>> live = new ArrayList<>(origins.size());
    for (final StorageOrigin origin : origins) {
      resolver
          .resolve(world, origin, ContainerRole.DEFAULT)
          .map(StorageProvider::storage)
          .ifPresent(live::add);
    }
    return List.copyOf(live);
  }
}
