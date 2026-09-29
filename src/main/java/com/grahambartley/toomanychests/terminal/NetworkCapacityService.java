package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
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
    return measure(world, terminal, role -> role != ContainerRole.EXCLUDED);
  }

  public int freeSlots(ServerWorld world, BlockPos terminal, ContainerRole role) {
    Objects.requireNonNull(role, "role");
    return measure(world, terminal, role::equals).freeSlots();
  }

  private NetworkCapacity measure(
      ServerWorld world, BlockPos terminal, Predicate<ContainerRole> counted) {
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (settings == null || network == null) {
      return NetworkCapacity.EMPTY;
    }
    int used = 0;
    int total = 0;
    for (final StorageOrigin origin : network.containers()) {
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      final ContainerRole role = settings.roleFor(block.pos());
      if (!counted.test(role)) {
        continue;
      }
      final Optional<StorageProvider> provider = resolver.resolve(world, block.pos(), role);
      if (provider.isEmpty()) {
        continue;
      }
      for (final StorageView<ItemVariant> view : provider.get().storage()) {
        total++;
        if (!view.isResourceBlank() && view.getAmount() > 0L) {
          used++;
        }
      }
    }
    return new NetworkCapacity(used, total);
  }
}
