package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.LoadedChunks;
import com.grahambartley.toomanychests.network.PayloadCapLog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class OwnedTerminals {
  public static final int MAX_LISTED = 64;

  private OwnedTerminals() {}

  public static List<OwnedTerminal> copyableBy(
      ServerWorld world, TerminalCaller caller, BlockPos excluding) {
    if (world == null || caller == null) {
      return List.of();
    }
    final List<OwnedTerminal> found = new ArrayList<>();
    for (final TerminalAddress address : TerminalNetworks.registry().addresses()) {
      if (!address.worldKey().equals(world.getRegistryKey())
          || address.pos().equals(excluding)
          || !LoadedChunks.holds(world, address.pos())) {
        continue;
      }
      final TerminalBlockEntity block = TerminalLookup.at(world, address.pos()).orElse(null);
      if (block == null || !caller.mayAdminister(block.settings())) {
        continue;
      }
      found.add(new OwnedTerminal(address.pos(), block.settings().tags().custom().size()));
    }
    if (found.size() > MAX_LISTED) {
      PayloadCapLog.reached("terminals to copy tags from", MAX_LISTED, found.size());
    }
    return nearestFirst(found, excluding);
  }

  private static List<OwnedTerminal> nearestFirst(List<OwnedTerminal> found, BlockPos origin) {
    final Comparator<OwnedTerminal> byDistance =
        origin == null
            ? Comparator.comparingLong(terminal -> terminal.pos().asLong())
            : Comparator.comparingDouble(terminal -> terminal.pos().getSquaredDistance(origin));
    return found.stream().sorted(byDistance).limit(MAX_LISTED).toList();
  }
}
