package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerNamingService {
  private final ContainerAssignmentWriter writer;

  public ContainerNamingService() {
    this(new ContainerAssignmentWriter());
  }

  public ContainerNamingService(ContainerAssignmentWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  public String nameOf(ServerWorld world, BlockPos terminal, BlockPos container) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(settings -> settings.nameFor(container))
        .orElse("");
  }

  public RoleChangeOutcome rename(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      BlockPos container,
      String name) {
    return writer.write(
        world, terminal, caller, container, assignment -> assignment.withName(name));
  }
}
