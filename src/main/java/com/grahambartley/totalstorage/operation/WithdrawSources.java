package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

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
    Objects.requireNonNull(world, "world");
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
