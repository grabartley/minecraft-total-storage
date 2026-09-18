package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.network.TagSetCopyPayload;
import com.grahambartley.toomanychests.network.TagSourcesPayload;
import com.grahambartley.toomanychests.network.TagSourcesRequestPayload;
import com.grahambartley.toomanychests.operation.TerminalNotice;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.tag.TagTransferResult;
import com.grahambartley.toomanychests.terminal.NetworkTagTransfer;
import com.grahambartley.toomanychests.terminal.OwnedTerminals;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalTagTransfers {
  private final TerminalTagPanelService panels;
  private final TerminalListingService listings;
  private final NetworkTagTransfer transfers;

  public TerminalTagTransfers(TerminalTagPanelService panels, TerminalListingService listings) {
    this(panels, listings, new NetworkTagTransfer());
  }

  public TerminalTagTransfers(
      TerminalTagPanelService panels,
      TerminalListingService listings,
      NetworkTagTransfer transfers) {
    this.panels = Objects.requireNonNull(panels, "panels");
    this.listings = Objects.requireNonNull(listings, "listings");
    this.transfers = Objects.requireNonNull(transfers, "transfers");
  }

  public boolean listSources(ServerPlayerEntity player, TagSourcesRequestPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    ServerPlayNetworking.send(
        player,
        new TagSourcesPayload(
            payload.syncId(),
            OwnedTerminals.copyableBy(
                context.world(), TerminalCaller.of(player), context.terminal())));
    return true;
  }

  public boolean copy(ServerPlayerEntity player, TagSetCopyPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    final TagTransferResult result =
        transfers.copy(
            context.world(),
            context.terminal(),
            payload.source(),
            TerminalCaller.of(player),
            payload.mode());
    report(player, result);
    if (!result.isChange()) {
      panels.refresh(player);
      return false;
    }
    pushed(context);
    return true;
  }

  private static void report(ServerPlayerEntity player, TagTransferResult result) {
    switch (result.outcome()) {
      case CHANGED ->
          TerminalNotice.tell(
              player, TerminalMessages.tagsCopied(result.imported(), result.skipped()));
      case UNCHANGED -> TerminalNotice.tell(player, TerminalMessages.tagsCopiedNothing());
      default -> TerminalNotice.tell(player, TerminalMessages.notYours());
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
