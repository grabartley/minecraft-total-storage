package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.network.TagEditPayload;
import com.grahambartley.toomanychests.operation.ExpensiveOperation;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.TagEdit;
import com.grahambartley.toomanychests.tag.TagEditOutcome;
import com.grahambartley.toomanychests.tag.TagEditResult;
import com.grahambartley.toomanychests.terminal.NetworkTagCandidates;
import com.grahambartley.toomanychests.terminal.NetworkTagEditor;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalTagEdits {
  private final TerminalTagPanelService panels;
  private final TerminalListingService listings;
  private final NetworkTagEditor tags;

  public TerminalTagEdits(TerminalTagPanelService panels, TerminalListingService listings) {
    this(panels, listings, new NetworkTagEditor());
  }

  public TerminalTagEdits(
      TerminalTagPanelService panels, TerminalListingService listings, NetworkTagEditor tags) {
    this.panels = Objects.requireNonNull(panels, "panels");
    this.listings = Objects.requireNonNull(listings, "listings");
    this.tags = Objects.requireNonNull(tags, "tags");
  }

  public boolean edit(ServerPlayerEntity player, TagEditPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    final TagEditResult result = apply(context, payload);
    report(player, payload.action(), result);
    if (!result.isChange()) {
      panels.refresh(player);
      return false;
    }
    pushed(context);
    return true;
  }

  private TagEditResult apply(TerminalOperationContext context, TagEditPayload payload) {
    final TerminalCaller caller = TerminalCaller.of(context.player());
    return switch (payload.action()) {
      case CREATE -> tags.create(context.world(), context.terminal(), caller, payload.name());
      case RENAME ->
          tags.rename(context.world(), context.terminal(), caller, payload.tagId(), payload.name());
      case DELETE -> tags.delete(context.world(), context.terminal(), caller, payload.tagId());
      case ASSIGN, UNASSIGN ->
          tags.assign(
              context.world(),
              context.terminal(),
              caller,
              payload.tagId(),
              NetworkTagCandidates.idOf(payload.item().orElse(null)),
              payload.action() == TagEdit.ASSIGN);
      case ASSIGN_MATCHING -> assignMatching(context, caller, payload.tagId());
    };
  }

  private TagEditResult assignMatching(
      TerminalOperationContext context, TerminalCaller caller, String tagId) {
    final TagEditOutcome refusal =
        tags.refusalFor(context.world(), context.terminal(), caller, tagId);
    if (refusal != null) {
      return TagEditResult.of(refusal);
    }
    if (!OperationGuards.allows(context.player(), ExpensiveOperation.ASSIGN_MATCHING)) {
      return TagEditResult.of(TagEditOutcome.UNCHANGED);
    }
    return tags.assignMatching(
        context.world(), context.terminal(), caller, tagId, context.handler().listing().filter());
  }

  private static void report(ServerPlayerEntity player, TagEdit action, TagEditResult result) {
    switch (result.outcome()) {
      case DENIED -> player.sendMessage(TerminalMessages.notYours(), true);
      case UNKNOWN_TAG -> player.sendMessage(TerminalMessages.tagUnknown(), true);
      case PRESET_IMMUTABLE -> player.sendMessage(TerminalMessages.tagIsPreset(), true);
      case TOO_MANY_TAGS ->
          player.sendMessage(TerminalMessages.tagLimitReached(NetworkTagSet.MAX_CUSTOM), true);
      case NAME_REJECTED ->
          player.sendMessage(TerminalMessages.tagNameRejected(result.name()), true);
      case CHANGED -> reportChange(player, action, result);
      default -> {}
    }
  }

  private static void reportChange(
      ServerPlayerEntity player, TagEdit action, TagEditResult result) {
    if (action == TagEdit.DELETE) {
      player.sendMessage(TerminalMessages.tagDeleted(result.affected()), true);
      return;
    }
    if (action == TagEdit.ASSIGN_MATCHING) {
      player.sendMessage(TerminalMessages.tagAssignedTo(result.affected()), true);
    }
  }

  private void pushed(TerminalOperationContext context) {
    final TerminalAddress terminal = TerminalAddress.of(context.world(), context.terminal());
    TerminalViewerSync.playersOn(context.world().getServer(), terminal)
        .forEach(
            viewer -> {
              panels.refresh(viewer);
              listings.refresh(viewer);
            });
  }
}
