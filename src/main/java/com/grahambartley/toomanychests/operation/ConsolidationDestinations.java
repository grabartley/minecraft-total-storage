package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

public final class ConsolidationDestinations {
  private final FilterCandidateFactory candidates;

  public ConsolidationDestinations() {
    this(new FilterCandidateFactory());
  }

  public ConsolidationDestinations(FilterCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public List<StorageOrigin> forVariant(
      NetworkOperationContext context, NetworkMembership network, ItemVariant variant) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(network, "network");
    if (variant == null || variant.isBlank()) {
      return List.of();
    }
    final NetworkIndexEntry<ItemVariant> held = network.index().entry(variant).orElse(null);
    if (held == null) {
      return List.of();
    }
    final TerminalSettings settings = context.settings();
    final FilterCandidate candidate = candidates.of(variant);
    final Map<BlockPos, StorageOrigin> byPosition = byPosition(network);
    final List<DepositTarget> targets = new ArrayList<>(byPosition.size());
    for (final Map.Entry<BlockPos, StorageOrigin> container : byPosition.entrySet()) {
      final ContainerRole role = settings.roleFor(container.getKey());
      final boolean holdsItem = held.isHeldBy(container.getValue());
      if (role != ContainerRole.FILTERED && !holdsItem) {
        continue;
      }
      targets.add(
          new DepositTarget(
              container.getKey(), role, settings.filterFor(container.getKey()), holdsItem));
    }
    return DepositRouting.order(
            targets,
            context.terminal(),
            candidate.identifier(),
            VariantTags.of(settings.tags(), candidate))
        .stream()
        .map(target -> byPosition.get(target.container()))
        .toList();
  }

  public List<StorageOrigin> holdersOf(
      NetworkOperationContext context, NetworkMembership network, ItemVariant variant) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(network, "network");
    final NetworkIndexEntry<ItemVariant> held = network.index().entry(variant).orElse(null);
    if (held == null) {
      return List.of();
    }
    final TerminalSettings settings = context.settings();
    final List<StorageOrigin> holders = new ArrayList<>(held.providerCount());
    for (final StorageOrigin origin : held.origins()) {
      if (!(origin instanceof BlockStorageOrigin block)
          || settings.roleFor(block.pos()) == ContainerRole.EXCLUDED) {
        continue;
      }
      holders.add(origin);
    }
    return List.copyOf(holders);
  }

  private static Map<BlockPos, StorageOrigin> byPosition(NetworkMembership network) {
    final Map<BlockPos, StorageOrigin> containers = new LinkedHashMap<>();
    for (final StorageOrigin origin : network.containers()) {
      if (origin instanceof BlockStorageOrigin block) {
        containers.put(block.pos(), origin);
      }
    }
    return containers;
  }
}
