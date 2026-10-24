package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import com.grahambartley.totalstorage.lifecycle.LoadedChunks;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.TagEditOutcome;
import com.grahambartley.totalstorage.tag.TagSetContents;
import com.grahambartley.totalstorage.tag.TagSetImport;
import com.grahambartley.totalstorage.tag.TagSetImportMode;
import com.grahambartley.totalstorage.tag.TagSetImportResult;
import com.grahambartley.totalstorage.tag.TagTransferResult;
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
    return importInto(world, target, caller, TagSetContents.of(lifted.get()), mode);
  }

  public TagTransferResult importInto(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      TagSetContents source,
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
    if (!LoadedChunks.holds(world, terminal)) {
      return null;
    }
    final TerminalBlockEntity block = TerminalLookup.at(world, terminal).orElse(null);
    if (block == null || caller == null || !caller.mayAdminister(block.settings())) {
      return null;
    }
    return block;
  }

  public static TagEditOutcome refusalFor(ServerWorld world, BlockPos terminal) {
    return LoadedChunks.holds(world, terminal) && TerminalLookup.at(world, terminal).isPresent()
        ? TagEditOutcome.DENIED
        : TagEditOutcome.NO_TERMINAL;
  }
}
