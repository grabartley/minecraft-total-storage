package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerAssignmentWriter {
  private final TerminalPower power;

  public ContainerAssignmentWriter() {
    this(TerminalNetworks.power());
  }

  public ContainerAssignmentWriter(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public RoleChangeOutcome write(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      BlockPos container,
      UnaryOperator<ContainerAssignment> change) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(container, "container");
    Objects.requireNonNull(change, "change");
    final TerminalBlockEntity blockEntity = TerminalLookup.at(world, terminal).orElse(null);
    if (blockEntity == null) {
      return RoleChangeOutcome.NO_TERMINAL;
    }
    if (caller == null || !caller.mayAdminister(blockEntity.settings())) {
      return RoleChangeOutcome.DENIED;
    }
    if (!reaches(world, terminal, container)) {
      return RoleChangeOutcome.NOT_IN_NETWORK;
    }
    final TerminalSettings before = blockEntity.settings();
    final ContainerAssignment updated = change.apply(before.assignmentFor(container));
    final TerminalSettings after =
        before.withContainers(
            before
                .containers()
                .with(container, updated == null ? ContainerAssignment.DEFAULT : updated));
    if (after.equals(before)) {
      return RoleChangeOutcome.UNCHANGED;
    }
    blockEntity.setSettings(after);
    return RoleChangeOutcome.CHANGED;
  }

  private boolean reaches(ServerWorld world, BlockPos terminal, BlockPos container) {
    return power
        .network(world, terminal)
        .map(network -> network.containers().contains(BlockStorageOrigin.of(world, container)))
        .orElse(false);
  }
}
