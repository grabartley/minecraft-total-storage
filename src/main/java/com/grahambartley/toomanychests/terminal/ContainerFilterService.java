package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterService {
  private final ContainerAssignmentWriter writer;

  public ContainerFilterService() {
    this(new ContainerAssignmentWriter());
  }

  public ContainerFilterService(ContainerAssignmentWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  public ContainerFilter filterOf(ServerWorld world, BlockPos terminal, BlockPos container) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(settings -> settings.filterFor(container))
        .orElse(ContainerFilter.NONE);
  }

  public RoleChangeOutcome accept(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      BlockPos container,
      String tagId,
      boolean accepted) {
    final String canonical = FilterText.tagId(tagId);
    if (canonical.isEmpty()) {
      return RoleChangeOutcome.UNCHANGED;
    }
    return writer.write(
        world,
        terminal,
        caller,
        container,
        assignment ->
            assignment.withFilter(
                accepted
                    ? assignment.filter().withTag(canonical)
                    : assignment.filter().withoutTag(canonical)));
  }

  public RoleChangeOutcome clear(
      ServerWorld world, BlockPos terminal, TerminalCaller caller, BlockPos container) {
    return writer.write(
        world,
        terminal,
        caller,
        container,
        assignment -> assignment.withFilter(ContainerFilter.NONE));
  }
}
