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
    return measureByRole(world, terminal).values().stream()
        .reduce(NetworkCapacity.EMPTY, NetworkCapacityService::combined);
  }

  public Map<ContainerRole, NetworkCapacity> measureByRole(ServerWorld world, BlockPos terminal) {
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (settings == null || network == null) {
      return Map.of();
    }
    final Map<ContainerRole, NetworkCapacity> byRole = new EnumMap<>(ContainerRole.class);
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
      byRole.merge(role, capacityOf(provider.get()), NetworkCapacityService::combined);
    }
    return Map.copyOf(byRole);
  }

  private static NetworkCapacity capacityOf(StorageProvider provider) {
    int used = 0;
    int total = 0;
    for (final StorageView<ItemVariant> view : provider.storage()) {
      total++;
      if (!view.isResourceBlank() && view.getAmount() > 0L) {
        used++;
      }
    }
    return new NetworkCapacity(used, total);
  }

  private static NetworkCapacity combined(NetworkCapacity first, NetworkCapacity second) {
    return new NetworkCapacity(
        first.usedSlots() + second.usedSlots(), first.totalSlots() + second.totalSlots());
  }
}
