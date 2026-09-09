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

public final class ContainerRoleService {
  private final StorageProviderResolver resolver;

  public ContainerRoleService() {
    this(new StorageProviderResolver());
  }

  public ContainerRoleService(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  public ContainerRole roleOf(ServerWorld world, BlockPos terminal, BlockPos container) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(settings -> settings.roleFor(container))
        .orElse(ContainerRole.DEFAULT);
  }

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
    int used = 0;
    int slots = 0;
    final Optional<StorageProvider> provider =
        resolver.resolve(world, container, settings.roleFor(container));
    if (provider.isPresent()) {
      for (final StorageView<ItemVariant> view : provider.get().storage()) {
        slots++;
        if (!view.isResourceBlank() && view.getAmount() > 0L) {
          used++;
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
        used,
        slots,
        terminal.getSquaredDistance(container));
  }

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
