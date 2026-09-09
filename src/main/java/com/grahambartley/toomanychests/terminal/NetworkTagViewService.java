package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalAccessRules;
import com.grahambartley.toomanychests.access.TerminalAccessService;
import com.grahambartley.toomanychests.network.PayloadCapLog;
import com.grahambartley.toomanychests.tag.NetworkTag;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.NetworkTagTally;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkTagViewService {

  public NetworkTagView viewFor(
      ServerPlayerEntity player, BlockPos terminal, Optional<ItemVariant> subject) {
    if (player == null || terminal == null || !(player.getWorld() instanceof ServerWorld world)) {
      return NetworkTagView.pending();
    }
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    if (settings == null) {
      return NetworkTagView.pending();
    }
    final NetworkTagSet tags = settings.tags();
    final NetworkTagTally tally =
        NetworkTagTally.of(NetworkTagCandidates.of(world, terminal, tags));
    return new NetworkTagView(
        TerminalAccessRules.mayAdminister(
            settings, player.getUuid(), TerminalAccessService.isOperator(player)),
        entriesOf(tags, tally),
        subject == null ? Optional.empty() : subject,
        subjectTags(tags, subject));
  }

  private static List<NetworkTagEntry> entriesOf(NetworkTagSet tags, NetworkTagTally tally) {
    final List<NetworkTagEntry> entries = new ArrayList<>(tags.all().size());
    for (final NetworkTag tag : tags.all()) {
      if (entries.size() >= NetworkTagView.MAX_TAGS) {
        PayloadCapLog.reached("network tags", NetworkTagView.MAX_TAGS, tags.all().size());
        break;
      }
      entries.add(
          new NetworkTagEntry(
              tag.id(),
              tag.displayName(),
              tag.immutable(),
              tag.items().size(),
              tally.countOf(tag.id())));
    }
    return List.copyOf(entries);
  }

  private static Set<String> subjectTags(NetworkTagSet tags, Optional<ItemVariant> subject) {
    if (subject == null || subject.isEmpty()) {
      return Set.of();
    }
    return new LinkedHashSet<>(tags.tagIdsFor(NetworkTagCandidates.idOf(subject.get())));
  }
}
