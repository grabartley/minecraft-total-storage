package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.network.TagSetCopyPayload;
import com.grahambartley.totalstorage.network.TagSourcesPayload;
import com.grahambartley.totalstorage.network.TagSourcesRequestPayload;
import com.grahambartley.totalstorage.operation.TerminalNotice;
import com.grahambartley.totalstorage.operation.TerminalOperationContext;
import com.grahambartley.totalstorage.operation.TerminalOperationGate;
import com.grahambartley.totalstorage.tag.TagTransferResult;
import com.grahambartley.totalstorage.terminal.NetworkTagTransfer;
import com.grahambartley.totalstorage.terminal.OwnedTerminals;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalMessages;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
      case CHANGED -> reportCopied(player, result);
      case UNCHANGED -> TerminalNotice.partial(player, TerminalMessages.tagsCopiedNothing());
      default -> TerminalNotice.failed(player, TerminalMessages.notYours());
    }
  }

  private static void reportCopied(ServerPlayerEntity player, TagTransferResult result) {
    final Text message = TerminalMessages.tagsCopied(result.imported(), result.skips());
    if (result.skips().isEmpty()) {
      TerminalNotice.done(player, message);
      return;
    }
    TerminalNotice.partial(player, TerminalMessages.tagsSkipped(result.skips().total()), message);
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
