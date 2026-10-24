package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
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
  private final ContainerAssignmentWriter writer;

  public ContainerRoleService() {
    this(new StorageProviderResolver(), new ContainerAssignmentWriter());
  }

  public ContainerRoleService(StorageProviderResolver resolver) {
    this(resolver, new ContainerAssignmentWriter());
  }

  public ContainerRoleService(StorageProviderResolver resolver, ContainerAssignmentWriter writer) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.writer = Objects.requireNonNull(writer, "writer");
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
    return writer.write(
        world,
        terminal,
        caller,
        container,
        assignment -> assignment.withRole(role == null ? ContainerRole.DEFAULT : role));
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
        settings.nameFor(container),
        settings.roleFor(container),
        List.copyOf(
            ContainerFilterService.onlyKnown(settings.filterFor(container), settings.tags())
                .tagIds()),
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
}
