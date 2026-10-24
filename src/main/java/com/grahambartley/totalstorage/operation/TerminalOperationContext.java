package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record TerminalOperationContext(
    ServerPlayerEntity player,
    ServerWorld world,
    TerminalScreenHandler handler,
    NetworkMembership network) {
  public TerminalOperationContext {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(handler, "handler");
    Objects.requireNonNull(network, "network");
  }

  public BlockPos terminal() {
    return handler.terminal();
  }
}
