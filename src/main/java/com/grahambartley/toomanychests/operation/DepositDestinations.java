package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.ArrayList;
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
 * <p>Whether a container already holds the item comes from the network index, which mutations keep
 * current. Being wrong about it costs a stack landing in the second-best container rather than
 * anything a player could lose.
 */
public final class DepositDestinations {
  private final StorageProviderResolver resolver;
  private final FilterCandidateFactory candidates;

  public DepositDestinations() {
    this(new StorageProviderResolver(), new FilterCandidateFactory());
  }

  public DepositDestinations(StorageProviderResolver resolver, FilterCandidateFactory candidates) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  /** Every container that will take this variant, in the order routing wants them tried. */
  public List<Storage<ItemVariant>> forVariant(
      ServerWorld world, BlockPos terminal, NetworkMembership network, ItemVariant variant) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(network, "network");
    if (variant == null || variant.isBlank()) {
      return List.of();
    }
    final TerminalSettings settings =
        TerminalLookup.settingsAt(world, terminal).orElseGet(TerminalSettings::defaults);
    final Map<BlockPos, StorageOrigin> origins = new LinkedHashMap<>();
    final List<DepositTarget> targets = new ArrayList<>();
    final Optional<NetworkIndexEntry<ItemVariant>> entry = network.index().entry(variant);
    for (final StorageOrigin origin : network.containers()) {
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      origins.put(block.pos(), origin);
      targets.add(
          new DepositTarget(
              block.pos(),
              settings.roleFor(block.pos()),
              settings.filterFor(block.pos()),
              entry.map(held -> held.isHeldBy(origin)).orElse(false)));
    }
    final FilterCandidate candidate = candidates.of(variant);
    final List<Storage<ItemVariant>> live = new ArrayList<>(targets.size());
    for (final DepositTarget target :
        DepositRouting.order(
            targets, terminal, candidate.identifier(), tagsFor(settings, candidate))) {
      resolver
          .resolve(world, origins.get(target.container()), ContainerRole.DEFAULT)
          .map(StorageProvider::storage)
          .ifPresent(live::add);
    }
    return List.copyOf(live);
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
