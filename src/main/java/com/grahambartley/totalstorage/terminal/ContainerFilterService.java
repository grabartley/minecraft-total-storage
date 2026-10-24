package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.filter.FilterText;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
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
    final NetworkTagSet tags = tagsOf(world, terminal);
    return writer.write(
        world,
        terminal,
        caller,
        container,
        assignment ->
            assignment.withFilter(edited(assignment.filter(), tags, canonical, accepted)));
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

  private static ContainerFilter edited(
      ContainerFilter filter, NetworkTagSet tags, String tagId, boolean accepted) {
    final ContainerFilter pruned = onlyKnown(filter, tags);
    if (tagId.isEmpty() || (accepted && !tags.contains(tagId))) {
      return pruned;
    }
    return accepted ? pruned.withTag(tagId) : pruned.withoutTag(tagId);
  }

  public static ContainerFilter onlyKnown(ContainerFilter filter, NetworkTagSet tags) {
    if (filter == null || filter.tagIds().isEmpty()) {
      return filter == null ? ContainerFilter.NONE : filter;
    }
    ContainerFilter kept = filter;
    for (final String tagId : filter.tagIds()) {
      if (!tags.contains(tagId)) {
        kept = kept.withoutTag(tagId);
      }
    }
    return kept;
  }

  private static NetworkTagSet tagsOf(ServerWorld world, BlockPos terminal) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(TerminalSettings::tags)
        .orElseGet(NetworkTagSet::defaults);
  }
}
