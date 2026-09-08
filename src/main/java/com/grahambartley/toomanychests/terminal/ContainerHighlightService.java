package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.network.ContainerHighlightPayload;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tells a player which containers their terminal is really holding, by outlining them in the world.
 *
 * <p>This is the answer to the one question a radius always produces: why is that chest not
 * included. Reading it off the world is immediate in a way a list of coordinates never is, and it
 * is why the highlight is worth a packet at all.
 *
 * <p>Sent to one player rather than broadcast. A highlight answers a question somebody just asked,
 * and painting outlines across a shared base for everyone nearby would be somebody else's screen
 * lighting up for no reason.
 *
 * <p>An unpowered terminal highlights nothing, because per ADR 0007 it is holding nothing. That is
 * a true answer rather than a missing one: the outlines a player does not see are the point.
 */
public final class ContainerHighlightService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerHighlightService.class);

  /** Long enough to walk a few steps and look around, short enough not to become an overlay. */
  public static final int TICKS = 100;

  private ContainerHighlightService() {}

  /** Returns how many containers were named, which is what a command reports. */
  public static int highlight(ServerPlayerEntity player, ServerWorld world, BlockPos terminal) {
    if (player == null || world == null || terminal == null) {
      return 0;
    }
    final List<HighlightedContainer> containers = containersOf(world, terminal);
    ServerPlayNetworking.send(player, new ContainerHighlightPayload(TICKS, containers));
    return containers.size();
  }

  /**
   * The containers a terminal holds, nearest first so the cap keeps the ones a player is standing
   * among rather than an arbitrary slice of a warehouse.
   */
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
      LOGGER.info(
          "Highlighting the nearest {} of {} containers at {}",
          ContainerHighlightPayload.MAX_CONTAINERS,
          positions.size(),
          terminal);
    }
    return positions.stream()
        .limit(ContainerHighlightPayload.MAX_CONTAINERS)
        .map(pos -> new HighlightedContainer(pos, settings.roleFor(pos)))
        .toList();
  }
}
