package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterEvaluator;
import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.filter.NetworkFilterCandidates;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkTagCandidates {
  private NetworkTagCandidates() {}

  public static List<FilterCandidate> of(ServerWorld world, BlockPos terminal, NetworkTagSet tags) {
    return of(world, terminal, tags, ItemFilter.NONE);
  }

  public static List<FilterCandidate> of(
      ServerWorld world, BlockPos terminal, NetworkTagSet tags, ItemFilter narrowing) {
    final NetworkMembership network =
        TerminalNetworks.power().network(world, terminal).orElse(null);
    if (network == null) {
      return List.of();
    }
    final ItemFilter filter = narrowing == null ? ItemFilter.NONE : narrowing;
    final NetworkFilterCandidates candidates =
        new NetworkFilterCandidates(tags == null ? NetworkTagSet.defaults() : tags);
    final List<FilterCandidate> resolved = new ArrayList<>(network.index().variantCount());
    for (final NetworkIndexEntry<ItemVariant> entry : network.index().entries()) {
      final FilterCandidate candidate = candidates.of(entry.variant());
      if (FilterEvaluator.strengthOf(candidate, filter).isMatch()) {
        resolved.add(candidate);
      }
    }
    return List.copyOf(resolved);
  }

  public static String idOf(ItemVariant variant) {
    return variant == null ? "" : Registries.ITEM.getId(variant.getItem()).toString();
  }
}
