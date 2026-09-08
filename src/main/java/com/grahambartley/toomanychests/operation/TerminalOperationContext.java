package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * One player, one open terminal, and the live network behind it, resolved together.
 *
 * <p>Handed out only by {@link TerminalOperationGate}, so holding one is proof that the permission
 * check, the screen check and the network lookup all passed for the same request. Nothing here is
 * held between ticks: a context describes the instant it was built and is rebuilt for the next
 * request rather than cached.
 */
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
