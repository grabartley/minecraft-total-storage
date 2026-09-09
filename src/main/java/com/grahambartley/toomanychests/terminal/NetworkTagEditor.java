package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterEvaluator;
import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.tag.NetworkTag;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.TagEditOutcome;
import com.grahambartley.toomanychests.tag.TagEditResult;
import com.grahambartley.toomanychests.tag.TagNameOutcome;
import com.grahambartley.toomanychests.tag.TagNameRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkTagEditor {

  public TagEditResult create(
      ServerWorld world, BlockPos terminal, TerminalCaller caller, String name) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagEditResult.of(refusalFor(world, terminal));
    }
    final NetworkTagSet tags = block.settings().tags();
    if (tags.custom().size() >= NetworkTagSet.MAX_CUSTOM) {
      return TagEditResult.of(TagEditOutcome.TOO_MANY_TAGS);
    }
    final TagNameOutcome check = TagNameRule.check(name, tags);
    if (!check.isAccepted()) {
      return TagEditResult.rejected(check);
    }
    return store(block, tags.withCustomTag(TagNameRule.idFor(name), name.trim()), 0);
  }

  public TagEditResult rename(
      ServerWorld world, BlockPos terminal, TerminalCaller caller, String tagId, String name) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagEditResult.of(refusalFor(world, terminal));
    }
    final NetworkTagSet tags = block.settings().tags();
    final TagEditOutcome editable = editableTag(tags, tagId);
    if (editable != null) {
      return TagEditResult.of(editable);
    }
    final TagNameOutcome check = TagNameRule.check(name, tags, tagId);
    if (!check.isAccepted()) {
      return TagEditResult.rejected(check);
    }
    return store(block, tags.withRenamedTag(tagId, name), 0);
  }

  public TagEditResult delete(
      ServerWorld world, BlockPos terminal, TerminalCaller caller, String tagId) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagEditResult.of(refusalFor(world, terminal));
    }
    final NetworkTagSet tags = block.settings().tags();
    final TagEditOutcome editable = editableTag(tags, tagId);
    if (editable != null) {
      return TagEditResult.of(editable);
    }
    final int carried = tags.byId(tagId).map(tag -> tag.items().size()).orElse(0);
    return store(block, tags.withoutTag(tagId), carried);
  }

  public TagEditResult assign(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      String tagId,
      String itemId,
      boolean assigned) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagEditResult.of(refusalFor(world, terminal));
    }
    final NetworkTagSet tags = block.settings().tags();
    final TagEditOutcome editable = editableTag(tags, tagId);
    if (editable != null) {
      return TagEditResult.of(editable);
    }
    if (FilterText.isBlank(itemId)) {
      return TagEditResult.of(TagEditOutcome.UNCHANGED);
    }
    final NetworkTagSet updated =
        assigned ? tags.withItem(tagId, itemId) : tags.withoutItem(tagId, itemId);
    return store(block, updated, 1);
  }

  public TagEditResult assignMatching(
      ServerWorld world,
      BlockPos terminal,
      TerminalCaller caller,
      String tagId,
      ItemFilter filter) {
    final TerminalBlockEntity block = administrable(world, terminal, caller);
    if (block == null) {
      return TagEditResult.of(refusalFor(world, terminal));
    }
    final NetworkTagSet tags = block.settings().tags();
    final TagEditOutcome editable = editableTag(tags, tagId);
    if (editable != null) {
      return TagEditResult.of(editable);
    }
    final NetworkTag existing = tags.byId(tagId).orElseThrow();
    final List<String> matching = new ArrayList<>();
    for (final FilterCandidate candidate : NetworkTagCandidates.of(world, terminal, tags)) {
      if (FilterEvaluator.strengthOf(candidate, filter == null ? ItemFilter.NONE : filter).isMatch()
          && !existing.contains(candidate.identifier())) {
        matching.add(candidate.identifier());
      }
    }
    return store(block, tags.withItems(tagId, matching), matching.size());
  }

  private static TagEditOutcome editableTag(NetworkTagSet tags, String tagId) {
    final Optional<NetworkTag> tag = tags.byId(tagId);
    if (tag.isEmpty()) {
      return TagEditOutcome.UNKNOWN_TAG;
    }
    return tag.get().immutable() ? TagEditOutcome.PRESET_IMMUTABLE : null;
  }

  private static TagEditResult store(
      TerminalBlockEntity block, NetworkTagSet updated, int affected) {
    final TerminalSettings before = block.settings();
    final TerminalSettings after = before.withTags(updated);
    if (after.equals(before)) {
      return TagEditResult.of(TagEditOutcome.UNCHANGED);
    }
    block.setSettings(after);
    return TagEditResult.changed(affected);
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
