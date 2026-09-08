package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.screenhandler.TerminalOpenGuard;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * The one door every client request to move items through a terminal comes in by.
 *
 * <p>Per ADR 0009 a request carries intent and a sync id, and neither is trusted. The sync id is
 * compared against the handler the server already holds for that player, so a packet naming a
 * terminal the sender never opened resolves to nothing. Permission is decided again here rather
 * than remembered from the screen opening, because a trust list edited while a screen is up has to
 * bind the very next click.
 *
 * <p>A request that fails any of these gets an empty answer rather than an exception. Refusal is an
 * ordinary outcome of a protocol where the client's view is always a little behind, and the caller
 * turns it into a resync.
 */
public final class TerminalOperationGate {

  private TerminalOperationGate() {}

  public static Optional<TerminalOperationContext> open(ServerPlayerEntity player, int syncId) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || handler.syncId != syncId
        || !(player.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    if (!TerminalOpenGuard.check(world, handler.terminal(), player).allowed()) {
      return Optional.empty();
    }
    return TerminalNetworks.power()
        .network(world, handler.terminal())
        .map(network -> new TerminalOperationContext(player, world, handler, network));
  }
}
