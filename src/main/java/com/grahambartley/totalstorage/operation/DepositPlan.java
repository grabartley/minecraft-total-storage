package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.filter.FilterCandidateFactory;
import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class DepositPlan {
  private final ServerWorld world;
  private final TerminalSettings settings;
  private final DepositOrder<ItemVariant> order;
  private final StorageProviderResolver resolver;
  private final FilterCandidateFactory candidates;
  private final Map<ItemVariant, Routing> routings = new HashMap<>();
  private final Map<StorageOrigin, Optional<Storage<ItemVariant>>> storages = new HashMap<>();

  DepositPlan(
      ServerWorld world,
      BlockPos terminal,
      TerminalSettings settings,
      Map<BlockPos, StorageOrigin> containers,
      NetworkIndex<ItemVariant> index,
      StorageProviderResolver resolver,
      FilterCandidateFactory candidates) {
    this.world = Objects.requireNonNull(world, "world");
    this.settings = Objects.requireNonNull(settings, "settings");
    this.order =
        new DepositOrder<>(terminal, containers, settings, new ContainerOccupancy<>(index));
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public List<StorageOrigin> destinationsFor(ItemVariant variant) {
    if (variant == null || variant.isBlank()) {
      return List.of();
    }
    final Routing routing = routings.computeIfAbsent(variant, this::routingOf);
    return order.forVariant(variant, routing.itemId(), routing.tags());
  }

  public Optional<Storage<ItemVariant>> storageOf(StorageOrigin container) {
    return storages.computeIfAbsent(
        container,
        key -> resolver.resolve(world, key, ContainerRole.DEFAULT).map(StorageProvider::storage));
  }

  public void stored(StorageOrigin container, ItemVariant variant) {
    order.stored(container, variant);
  }

  private Routing routingOf(ItemVariant variant) {
    final FilterCandidate candidate = candidates.of(variant);
    return new Routing(candidate.identifier(), VariantTags.of(settings.tags(), candidate));
  }

  private record Routing(String itemId, Set<String> tags) {}
}
