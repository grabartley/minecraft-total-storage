package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Turns {@link DepositRouting}'s decision into the live containers a deposit will really write to.
 *
 * <p>The decision itself is plain arithmetic over roles and positions; this is the half that has to
 * touch the world, so it is kept apart from it. Everything it reads is read now: the roles from the
 * terminal's own settings, the tags from its tag set, and the storages from the blocks themselves,
 * so a container broken or reassigned since the last scan is not written to.
 *
 * <p>Several variants are routed together rather than one at a time, because the terminal's
 * settings and the set of containers are the same for all of them. Emptying a full inventory means
 * routing dozens of variants at once, and asking each of them to walk the network again would make
 * the mod's headline gesture the most expensive thing it does.
 *
 * <p>Whether a container already holds an item comes from the network index, which mutations keep
 * current. Being wrong about it costs a stack landing in the second-best container rather than
 * anything a player could lose.
 */
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

  /**
   * Every container that will take each variant, in the order routing wants them tried, reading the
   * terminal and its containers once for all of them.
   */
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
                targets, terminal, candidate.identifier(), tagsFor(settings, candidate))
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

  /**
   * Every tag this network files the item under. Presets are computed per ADR 0011 and custom tags
   * are stored on the terminal, so a filter naming either kind has to be matched against both.
   */
  private static Set<String> tagsFor(TerminalSettings settings, FilterCandidate candidate) {
    final Set<String> filed = new LinkedHashSet<>(candidate.tags());
    filed.addAll(settings.tags().tagIdsFor(candidate.identifier()));
    return Set.copyOf(filed);
  }
}
