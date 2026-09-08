package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Reads and moves the role one terminal has given one container. See ADR 0004.
 *
 * <p>A role may only be given to a container the terminal's network actually reaches. That check is
 * the reason this is a service rather than a setter: without it a request could write an assignment
 * for any position in the world, and a save would slowly fill with opinions about containers that
 * were never part of anything.
 *
 * <p>Naming a container by position is not the thing ADR 0009 forbids. That rule is about item
 * movement, where a supplied position would decide where items come from; here the position is the
 * subject of the change rather than a shortcut to one, and it is validated against the live network
 * before anything is written.
 */
public final class ContainerRoleService {
  private final StorageProviderResolver resolver;

  public ContainerRoleService() {
    this(new StorageProviderResolver());
  }

  public ContainerRoleService(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  /** What this terminal has decided about a container, defaulting for one it has never seen. */
  public ContainerRole roleOf(ServerWorld world, BlockPos terminal, BlockPos container) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(settings -> settings.roleFor(container))
        .orElse(ContainerRole.DEFAULT);
  }

  /**
   * Gives a container a role, if the caller may configure the terminal and the terminal can see the
   * container.
   */
  public RoleChangeOutcome assign(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      BlockPos container,
      ContainerRole role) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(container, "container");
    final TerminalBlockEntity blockEntity = TerminalLookup.at(world, terminal).orElse(null);
    if (blockEntity == null) {
      return RoleChangeOutcome.NO_TERMINAL;
    }
    if (caller == null || !caller.mayAdminister(blockEntity.settings())) {
      return RoleChangeOutcome.DENIED;
    }
    if (!reaches(world, terminal, container)) {
      return RoleChangeOutcome.NOT_IN_NETWORK;
    }
    final TerminalSettings before = blockEntity.settings();
    final TerminalSettings after =
        before.withContainerRole(container, role == null ? ContainerRole.DEFAULT : role);
    if (after.equals(before)) {
      return RoleChangeOutcome.UNCHANGED;
    }
    blockEntity.setSettings(after);
    return RoleChangeOutcome.CHANGED;
  }

  /**
   * Every container this terminal can see right now, nearest first, with what it holds.
   *
   * <p>Ordered by distance because that is the order a player walked past them in, and a list of
   * fifty coordinates in any other order is a list nobody can find anything in.
   */
  public List<ContainerRoleEntry> containers(ServerWorld world, BlockPos terminal) {
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (settings == null || network == null) {
      return List.of();
    }
    final List<ContainerRoleEntry> rows = new ArrayList<>(network.containers().size());
    for (final StorageOrigin origin : network.containers()) {
      if (origin instanceof BlockStorageOrigin block) {
        rows.add(entryFor(world, terminal, settings, block.pos()));
      }
    }
    rows.sort(
        Comparator.comparingDouble(ContainerRoleEntry::distanceSq)
            .thenComparingLong(row -> row.container().asLong()));
    return List.copyOf(rows);
  }

  public RoleTally tally(ServerWorld world, BlockPos terminal) {
    return RoleTally.of(
        containers(world, terminal).stream().map(ContainerRoleEntry::role).toList());
  }

  private ContainerRoleEntry entryFor(
      ServerWorld world, BlockPos terminal, TerminalSettings settings, BlockPos container) {
    final Map<ItemVariant, Long> held = new LinkedHashMap<>();
    long items = 0L;
    final Optional<StorageProvider> provider =
        resolver.resolve(world, container, settings.roleFor(container));
    if (provider.isPresent()) {
      for (final StorageView<ItemVariant> view : provider.get().storage()) {
        if (!view.isResourceBlank() && view.getAmount() > 0L) {
          held.merge(view.getResource(), view.getAmount(), Long::sum);
          items += view.getAmount();
        }
      }
    }
    return new ContainerRoleEntry(
        container,
        settings.roleFor(container),
        commonest(held),
        held.size(),
        items,
        terminal.getSquaredDistance(container));
  }

  /**
   * The variant a container holds most of, which is what the row draws to be recognisable. Ties go
   * to whichever the container listed first, so the same chest draws the same icon twice running.
   */
  private static Optional<ItemVariant> commonest(Map<ItemVariant, Long> held) {
    return held.entrySet().stream()
        .max(Comparator.comparingLong(Map.Entry::getValue))
        .map(Map.Entry::getKey);
  }

  private static boolean reaches(ServerWorld world, BlockPos terminal, BlockPos container) {
    return TerminalNetworks.power()
        .network(world, terminal)
        .map(network -> network.containers().contains(BlockStorageOrigin.of(world, container)))
        .orElse(false);
  }
}
