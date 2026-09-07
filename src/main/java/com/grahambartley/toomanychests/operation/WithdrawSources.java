package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

/**
 * The containers a withdrawal will really take from, resolved the moment it is about to take.
 *
 * <p>The index says where a variant lives, and per ADR 0009 that is a hint rather than a promise.
 * Every origin it names is looked up in the world again, so a container broken since the index was
 * built drops out instead of being extracted from, and a stale hint costs a shortfall rather than
 * an item that never existed.
 *
 * <p>Roles are not consulted. Every role is withdrawable per ADR 0004, {@code EXCLUDED} included:
 * what that role protects against is unwanted writes, and a container nobody could take from would
 * be a container nobody could use.
 */
public final class WithdrawSources {
  private final LiveStorages live;

  public WithdrawSources() {
    this(new LiveStorages());
  }

  public WithdrawSources(LiveStorages live) {
    this.live = Objects.requireNonNull(live, "live");
  }

  public List<Storage<ItemVariant>> holding(
      ServerWorld world, NetworkMembership network, ItemVariant variant) {
    Objects.requireNonNull(network, "network");
    if (variant == null || variant.isBlank()) {
      return List.of();
    }
    return network
        .index()
        .entry(variant)
        .map(entry -> live.of(world, entry.origins()))
        .orElseGet(List::of);
  }
}
