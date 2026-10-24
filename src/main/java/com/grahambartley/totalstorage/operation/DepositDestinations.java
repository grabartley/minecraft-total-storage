package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.filter.FilterCandidateFactory;
import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class DepositDestinations {
  private final LiveStorages live;
  private final FilterCandidateFactory candidates;

  public DepositDestinations() {
    this(new LiveStorages(), new FilterCandidateFactory());
  }

  public DepositDestinations(LiveStorages live, FilterCandidateFactory candidates) {
    this.live = Objects.requireNonNull(live, "live");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public Map<ItemVariant, List<Storage<ItemVariant>>> forVariants(
      ServerWorld world,
      BlockPos terminal,
      NetworkMembership network,
      Collection<ItemVariant> variants) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(network, "network");
    if (variants == null || variants.isEmpty()) {
      return Map.of();
    }
    final TerminalSettings settings =
        TerminalLookup.settingsAt(world, terminal).orElseGet(TerminalSettings::defaults);
    final Map<BlockPos, StorageOrigin> containers = containersOf(network);
    final NetworkIndex<ItemVariant> index = network.index();
    final Map<ItemVariant, List<Storage<ItemVariant>>> routed = new LinkedHashMap<>();
    for (final ItemVariant variant : variants) {
      if (variant == null || variant.isBlank() || routed.containsKey(variant)) {
        continue;
      }
      routed.put(variant, order(world, terminal, settings, containers, index, variant));
    }
    return Map.copyOf(routed);
  }

  private List<Storage<ItemVariant>> order(
      ServerWorld world,
      BlockPos terminal,
      TerminalSettings settings,
      Map<BlockPos, StorageOrigin> containers,
      NetworkIndex<ItemVariant> index,
      ItemVariant variant) {
    final FilterCandidate candidate = candidates.of(variant);
    final Optional<NetworkIndexEntry<ItemVariant>> held = index.entry(variant);
    final List<DepositTarget> targets = new ArrayList<>(containers.size());
    for (final Map.Entry<BlockPos, StorageOrigin> container : containers.entrySet()) {
      targets.add(
          new DepositTarget(
              container.getKey(),
              settings.roleFor(container.getKey()),
              settings.filterFor(container.getKey()),
              held.map(entry -> entry.isHeldBy(container.getValue())).orElse(false)));
    }
    return live.of(
        world,
        DepositRouting.order(
                targets,
                terminal,
                candidate.identifier(),
                VariantTags.of(settings.tags(), candidate))
            .stream()
            .map(target -> containers.get(target.container()))
            .toList());
  }

  private static Map<BlockPos, StorageOrigin> containersOf(NetworkMembership network) {
    final Map<BlockPos, StorageOrigin> containers = new LinkedHashMap<>();
    for (final StorageOrigin origin : network.containers()) {
      if (origin instanceof BlockStorageOrigin block) {
        containers.put(block.pos(), origin);
      }
    }
    return containers;
  }
}
