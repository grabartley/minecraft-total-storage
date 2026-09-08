package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

/**
 * Turns the places a network remembers into the containers that are really there right now.
 *
 * <p>Per ADR 0009 a recorded origin is a hint rather than a promise, and both halves of an
 * operation depend on the same treatment of it: look it up in the world again, and drop it silently
 * if the container has gone. Holding that in one place is what stops a withdrawal and a deposit
 * disagreeing about whether a broken chest is still part of the network.
 *
 * <p>Nothing here is held between ticks. What it returns describes the instant it was asked.
 */
public final class LiveStorages {
  private final StorageProviderResolver resolver;

  public LiveStorages() {
    this(new StorageProviderResolver());
  }

  public LiveStorages(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  /** The containers still standing, in the order the origins were given. */
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
