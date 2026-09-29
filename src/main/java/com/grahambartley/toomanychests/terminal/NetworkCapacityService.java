package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkCapacityService {
  private final StorageProviderResolver resolver;

  public NetworkCapacityService() {
    this(new StorageProviderResolver());
  }

  public NetworkCapacityService(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  public NetworkCapacity measure(ServerWorld world, BlockPos terminal) {
    final Map<ContainerRole, NetworkCapacity> byRole = measureByRole(world, terminal);
    int used = 0;
    int total = 0;
    for (final NetworkCapacity capacity : byRole.values()) {
      used += capacity.usedSlots();
      total += capacity.totalSlots();
    }
    return new NetworkCapacity(used, total);
  }

  public Map<ContainerRole, NetworkCapacity> measureByRole(ServerWorld world, BlockPos terminal) {
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (settings == null || network == null) {
      return Map.of();
    }
    final Map<ContainerRole, int[]> tallies = new EnumMap<>(ContainerRole.class);
    for (final StorageOrigin origin : network.containers()) {
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      final ContainerRole role = settings.roleFor(block.pos());
      if (role == ContainerRole.EXCLUDED) {
        continue;
      }
      final Optional<StorageProvider> provider = resolver.resolve(world, block.pos(), role);
      if (provider.isEmpty()) {
        continue;
      }
      final int[] tally = tallies.computeIfAbsent(role, unused -> new int[2]);
      for (final StorageView<ItemVariant> view : provider.get().storage()) {
        tally[1]++;
        if (!view.isResourceBlank() && view.getAmount() > 0L) {
          tally[0]++;
        }
      }
    }
    final Map<ContainerRole, NetworkCapacity> byRole = new EnumMap<>(ContainerRole.class);
    tallies.forEach((role, tally) -> byRole.put(role, new NetworkCapacity(tally[0], tally[1])));
    return Map.copyOf(byRole);
  }
}
