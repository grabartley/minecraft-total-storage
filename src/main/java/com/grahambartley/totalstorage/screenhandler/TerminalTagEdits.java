package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.network.TagEditPayload;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.operation.TerminalNotice;
import com.grahambartley.totalstorage.operation.TerminalOperationContext;
import com.grahambartley.totalstorage.operation.TerminalOperationGate;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.TagEdit;
import com.grahambartley.totalstorage.tag.TagEditResult;
import com.grahambartley.totalstorage.terminal.NetworkTagCandidates;
import com.grahambartley.totalstorage.terminal.NetworkTagEditor;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalMessages;
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
    return tags.assignMatching(
        context.world(),
        context.terminal(),
        caller,
        tagId,
        context.handler().listing().filter(),
        () -> OperationGuards.allows(context.player(), ExpensiveOperation.ASSIGN_MATCHING));
  }

  private static void report(ServerPlayerEntity player, TagEdit action, TagEditResult result) {
    switch (result.outcome()) {
      case DENIED -> TerminalNotice.tell(player, TerminalMessages.notYours());
      case UNKNOWN_TAG -> TerminalNotice.tell(player, TerminalMessages.tagUnknown());
      case PRESET_IMMUTABLE -> TerminalNotice.tell(player, TerminalMessages.tagIsPreset());
      case TOO_MANY_TAGS ->
          TerminalNotice.tell(player, TerminalMessages.tagLimitReached(NetworkTagSet.MAX_CUSTOM));
      case NAME_REJECTED ->
          TerminalNotice.tell(player, TerminalMessages.tagNameRejected(result.name()));
      case CHANGED -> reportChange(player, action, result);
      default -> {}
    }
  }

  private static void reportChange(
      ServerPlayerEntity player, TagEdit action, TagEditResult result) {
    if (action == TagEdit.DELETE) {
      TerminalNotice.tell(player, TerminalMessages.tagDeleted(result.affected()));
      return;
    }
    if (action == TagEdit.ASSIGN_MATCHING) {
      TerminalNotice.tell(player, TerminalMessages.tagAssignedTo(result.affected()));
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
