package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.network.ContainerHighlightPayload;
import com.grahambartley.toomanychests.network.PayloadCapLog;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerHighlightService {
  public static final int TICKS = 100;

  private ContainerHighlightService() {}

  public static int highlight(ServerPlayerEntity player, ServerWorld world, BlockPos terminal) {
    if (player == null || world == null || terminal == null) {
      return 0;
    }
    final List<HighlightedContainer> containers = containersOf(world, terminal);
    ServerPlayNetworking.send(player, new ContainerHighlightPayload(TICKS, containers));
    return containers.size();
  }

  public static List<HighlightedContainer> containersOf(ServerWorld world, BlockPos terminal) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    if (network == null || settings == null) {
      return List.of();
    }
    final List<BlockPos> positions = new ArrayList<>(network.containers().size());
    for (final StorageOrigin origin : network.containers()) {
      if (origin instanceof BlockStorageOrigin block) {
        positions.add(block.pos());
      }
    }
    positions.sort(
        Comparator.<BlockPos>comparingDouble(terminal::getSquaredDistance)
            .thenComparingLong(BlockPos::asLong));
    if (positions.size() > ContainerHighlightPayload.MAX_CONTAINERS) {
      PayloadCapLog.reached(
          "highlighted containers", ContainerHighlightPayload.MAX_CONTAINERS, positions.size());
    }
    return positions.stream()
        .limit(ContainerHighlightPayload.MAX_CONTAINERS)
        .map(pos -> new HighlightedContainer(pos, settings.roleFor(pos)))
        .toList();
  }
}
