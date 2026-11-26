package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

public final class DepositOrder<V> {
  private final BlockPos terminal;
  private final Map<BlockPos, StorageOrigin> containers;
  private final TerminalSettings settings;
  private final ContainerOccupancy<V> occupancy;
  private final Map<V, List<StorageOrigin>> orders = new HashMap<>();

  public DepositOrder(
      BlockPos terminal,
      Map<BlockPos, StorageOrigin> containers,
      TerminalSettings settings,
      ContainerOccupancy<V> occupancy) {
    this.terminal = Objects.requireNonNull(terminal, "terminal");
    this.containers = Objects.requireNonNull(containers, "containers");
    this.settings = Objects.requireNonNull(settings, "settings");
    this.occupancy = Objects.requireNonNull(occupancy, "occupancy");
  }

  public List<StorageOrigin> forVariant(V variant, String itemId, Collection<String> itemTagIds) {
    Objects.requireNonNull(variant, "variant");
    return orders.computeIfAbsent(variant, key -> order(key, itemId, itemTagIds));
  }

  public void stored(StorageOrigin container, V variant) {
    if (occupancy.stored(container, variant)) {
      orders.clear();
    }
  }

  private List<StorageOrigin> order(V variant, String itemId, Collection<String> itemTagIds) {
    final List<DepositTarget> targets = new ArrayList<>(containers.size());
    for (final Map.Entry<BlockPos, StorageOrigin> container : containers.entrySet()) {
      targets.add(
          new DepositTarget(
              container.getKey(),
              settings.roleFor(container.getKey()),
              settings.filterFor(container.getKey()),
              occupancy.contentsOf(container.getValue(), variant)));
    }
    return DepositRouting.order(targets, terminal, itemId, itemTagIds).stream()
        .map(target -> containers.get(target.container()))
        .toList();
  }
}
