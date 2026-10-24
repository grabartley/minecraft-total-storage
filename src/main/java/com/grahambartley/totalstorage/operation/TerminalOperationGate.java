package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.screenhandler.TerminalOpenGuard;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

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
