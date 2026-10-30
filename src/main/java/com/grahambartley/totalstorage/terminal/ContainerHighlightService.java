package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.lifecycle.NextTick;
import com.grahambartley.totalstorage.network.ContainerHighlightPayload;
import com.grahambartley.totalstorage.network.PayloadCapLog;
import com.grahambartley.totalstorage.storage.ScanRegion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerHighlightService {
  public static final int TICKS = 100;

  private static final int LOCATE_TICKS = 200;

  private static final double NEIGHBOUR_REACH = 1.0;

  private ContainerHighlightService() {}

  public static int highlight(ServerPlayerEntity player, ServerWorld world, BlockPos terminal) {
    if (player == null || world == null || terminal == null) {
      return 0;
    }
    final ContainerHighlightPayload payload =
        ContainerHighlightPayload.ofNetwork(TICKS, containersOf(world, terminal));
    ServerPlayNetworking.send(player, payload);
    return payload.containers().size();
  }

  public static void locate(ServerPlayerEntity player, HighlightedContainer container) {
    ServerPlayNetworking.send(player, locatedPayload(container));
  }

  public static ContainerHighlightPayload locatedPayload(HighlightedContainer container) {
    return ContainerHighlightPayload.ofOne(LOCATE_TICKS, container);
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
    final OutlinedNetwork outlined = OutlinedNetwork.at(world, terminal).orElse(null);
    if (outlined == null) {
      return List.of();
    }
    final List<ServerPlayerEntity> nearby =
        world.getPlayers(
            player ->
                isNear(player, outlined.network().region(), terminal)
                    && TerminalCaller.of(player).mayOpen(outlined.settings()));
    if (nearby.isEmpty()) {
      return List.of();
    }
    final ContainerHighlightPayload payload =
        ContainerHighlightPayload.ofNetwork(TICKS, containersOf(outlined, terminal));
    nearby.forEach(player -> ServerPlayNetworking.send(player, payload));
    return List.copyOf(nearby);
  }

  private static boolean isNear(ServerPlayerEntity player, ScanRegion region, BlockPos terminal) {
    return region.contains(player.getBlockPos())
        || player.canInteractWithBlockAt(terminal, NEIGHBOUR_REACH);
  }

  public static List<HighlightedContainer> containersOf(ServerWorld world, BlockPos terminal) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    return OutlinedNetwork.at(world, terminal)
        .map(outlined -> containersOf(outlined, terminal))
        .orElse(List.of());
  }

  private static List<HighlightedContainer> containersOf(
      OutlinedNetwork outlined, BlockPos terminal) {
    final List<BlockPos> positions =
        new ArrayList<>(outlined.network().containersByPosition().keySet());
    positions.sort(
        Comparator.<BlockPos>comparingDouble(terminal::getSquaredDistance)
            .thenComparingLong(BlockPos::asLong));
    if (positions.size() > ContainerHighlightPayload.MAX_CONTAINERS) {
      PayloadCapLog.reached(
          "highlighted containers", ContainerHighlightPayload.MAX_CONTAINERS, positions.size());
    }
    return positions.stream()
        .limit(ContainerHighlightPayload.MAX_CONTAINERS)
        .map(pos -> new HighlightedContainer(pos, outlined.settings().roleFor(pos)))
        .toList();
  }

  private record OutlinedNetwork(NetworkMembership network, TerminalSettings settings) {
    private static Optional<OutlinedNetwork> at(ServerWorld world, BlockPos terminal) {
      return TerminalNetworks.power()
          .network(world, terminal)
          .flatMap(
              network ->
                  TerminalLookup.settingsAt(world, terminal)
                      .map(settings -> new OutlinedNetwork(network, settings)));
    }
  }

  public record PowerOnHighlight(TerminalAddress terminal) {}
}
