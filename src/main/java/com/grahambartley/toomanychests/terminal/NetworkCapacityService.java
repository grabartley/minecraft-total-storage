package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Counts the room a network has left, by reading its containers rather than its index.
 *
 * <p>The index knows what the network holds; it does not know how many slots that is spread across
 * or how many empty ones remain beside it. Those are facts about the containers themselves, and a
 * count derived from item totals would be a guess that is wrong in exactly the case the readout
 * exists for: many kinds of item, few of each.
 *
 * <p>Read live, on request, rather than cached. The number is drawn on a panel a player has open,
 * so it is asked for at the rate a screen refreshes rather than at the rate items move, and a cache
 * would be a second thing every mutation had to remember to invalidate.
 */
public final class NetworkCapacityService {
  private final StorageProviderResolver resolver;

  public NetworkCapacityService() {
    this(new StorageProviderResolver());
  }

  public NetworkCapacityService(StorageProviderResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  /** The room left across every container this terminal may write to. */
  public NetworkCapacity of(ServerWorld world, BlockPos terminal, NetworkMembership network) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    if (network == null) {
      return NetworkCapacity.EMPTY;
    }
    final TerminalSettings settings =
        TerminalLookup.settingsAt(world, terminal).orElseGet(TerminalSettings::defaults);
    int used = 0;
    int total = 0;
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
