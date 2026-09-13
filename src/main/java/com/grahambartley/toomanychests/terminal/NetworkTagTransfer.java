package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.TagEditOutcome;
import com.grahambartley.toomanychests.tag.TagSetImport;
import com.grahambartley.toomanychests.tag.TagSetImportMode;
import com.grahambartley.toomanychests.tag.TagSetImportResult;
import com.grahambartley.toomanychests.tag.TagTransferResult;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkTagTransfer {
  public Optional<NetworkTagSet> read(ServerWorld world, BlockPos terminal, TerminalCaller caller) {
    return Optional.ofNullable(administrable(world, terminal, caller))
        .map(block -> block.settings().tags());
  }

  public TagTransferResult copy(
      ServerWorld world,
      BlockPos target,
      BlockPos source,
      TerminalCaller caller,
      TagSetImportMode mode) {
    final Optional<NetworkTagSet> lifted = read(world, source, caller);
    if (lifted.isEmpty()) {
      return TagTransferResult.of(refusalFor(world, source));
    }
    return importInto(world, target, caller, lifted.get(), mode);
  }

  public TagTransferResult importInto(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      NetworkTagSet source,
      TagSetImportMode mode) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagTransferResult.of(refusalFor(world, terminal));
    }
    final TagSetImportResult result = TagSetImport.into(block.settings().tags(), source, mode);
    return TagTransferResult.from(result, store(block, result.tags()));
  }

  private static boolean store(TerminalBlockEntity block, NetworkTagSet updated) {
    final TerminalSettings before = block.settings();
    final TerminalSettings after = before.withTags(updated);
    if (after.equals(before)) {
      return false;
    }
    block.setSettings(after);
    return true;
  }

  private static TerminalBlockEntity administrable(
      ServerWorld world, BlockPos terminal, TerminalCaller caller) {
    final TerminalBlockEntity block = TerminalLookup.at(world, terminal).orElse(null);
    if (block == null || caller == null || !caller.mayAdminister(block.settings())) {
      return null;
    }
    return block;
  }

  private static TagEditOutcome refusalFor(ServerWorld world, BlockPos terminal) {
    return TerminalLookup.at(world, terminal).isPresent()
        ? TagEditOutcome.DENIED
        : TagEditOutcome.NO_TERMINAL;
  }
}
