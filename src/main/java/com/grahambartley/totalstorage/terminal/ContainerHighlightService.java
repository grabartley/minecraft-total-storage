package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.lifecycle.NextTick;
import com.grahambartley.totalstorage.network.ContainerHighlightPayload;
import com.grahambartley.totalstorage.network.PayloadCapLog;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ScanRegion;
import com.grahambartley.totalstorage.storage.StorageOrigin;
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

  private static final double NEIGHBOUR_REACH = 1.0;

  private ContainerHighlightService() {}

  public static int highlight(ServerPlayerEntity player, ServerWorld world, BlockPos terminal) {
    if (player == null || world == null || terminal == null) {
      return 0;
    }
    final ContainerHighlightPayload payload = payloadFor(world, terminal);
    ServerPlayNetworking.send(player, payload);
    return payload.containers().size();
  }

  public static void poweredOn(ServerWorld world, BlockPos terminal) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    final BlockPos pos = terminal.toImmutable();
    NextTick.queue()
        .submit(
            new PowerOnHighlight(TerminalAddress.of(world, pos)),
            () -> highlightForEveryoneNearby(world, pos));
  }

  public static List<ServerPlayerEntity> highlightForEveryoneNearby(
      ServerWorld world, BlockPos terminal) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (network == null) {
      return List.of();
    }
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    if (settings == null) {
      return List.of();
    }
    final List<ServerPlayerEntity> nearby =
        world.getPlayers(
            player ->
                isNear(player, network.region(), terminal)
                    && TerminalCaller.of(player).mayOpen(settings));
    if (nearby.isEmpty()) {
      return List.of();
    }
    final ContainerHighlightPayload payload = payloadFor(world, terminal);
    nearby.forEach(player -> ServerPlayNetworking.send(player, payload));
    return List.copyOf(nearby);
  }

  private static ContainerHighlightPayload payloadFor(ServerWorld world, BlockPos terminal) {
    return new ContainerHighlightPayload(TICKS, containersOf(world, terminal));
  }

  private static boolean isNear(ServerPlayerEntity player, ScanRegion region, BlockPos terminal) {
    return region.contains(player.getBlockPos())
        || player.canInteractWithBlockAt(terminal, NEIGHBOUR_REACH);
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

  public record PowerOnHighlight(TerminalAddress terminal) {}
}
