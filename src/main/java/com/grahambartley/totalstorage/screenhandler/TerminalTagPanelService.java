package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.filter.ItemFilter;
import com.grahambartley.totalstorage.network.TagPanelPayload;
import com.grahambartley.totalstorage.network.TagPanelRequestPayload;
import com.grahambartley.totalstorage.operation.TerminalOperationContext;
import com.grahambartley.totalstorage.operation.TerminalOperationGate;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import com.grahambartley.totalstorage.terminal.NetworkTagViewService;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalTagPanelService {
  private final NetworkTagViewService views;

  public TerminalTagPanelService() {
    this(new NetworkTagViewService());
  }

  public TerminalTagPanelService(NetworkTagViewService views) {
    this.views = Objects.requireNonNull(views, "views");
  }

  public boolean request(ServerPlayerEntity player, TagPanelRequestPayload payload) {
    if (payload == null) {
      return false;
    }
    if (!payload.open()) {
      return closed(player, payload.syncId());
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    context.handler().tags().requested(true, payload.subject());
    send(player, context.handler(), viewFor(player, context.handler()));
    return true;
  }

  public boolean refresh(ServerPlayerEntity player) {
    final TerminalScreenHandler handler = openPanelOf(player);
    if (handler == null) {
      return false;
    }
    send(player, handler, viewFor(player, handler));
    return true;
  }

  public boolean contentsChanged(ServerPlayerEntity player) {
    final TerminalScreenHandler handler = openPanelOf(player);
    if (handler == null) {
      return false;
    }
    final NetworkTagView view = viewFor(player, handler);
    if (!handler.tags().differsFromLastSent(view)) {
      return false;
    }
    send(player, handler, view);
    return true;
  }

  private static TerminalScreenHandler openPanelOf(ServerPlayerEntity player) {
    return player != null
            && player.currentScreenHandler instanceof TerminalScreenHandler handler
            && handler.tags().isOpen()
        ? handler
        : null;
  }

  public NetworkTagView viewFor(ServerPlayerEntity player, TerminalScreenHandler handler) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(handler, "handler");
    return views.viewFor(player, handler.terminal(), handler.tags().subject(), searchOf(handler));
  }

  private static ItemFilter searchOf(TerminalScreenHandler handler) {
    return ItemFilter.ofQuery(handler.listing().filter().query());
  }

  private static void send(
      ServerPlayerEntity player, TerminalScreenHandler handler, NetworkTagView view) {
    ServerPlayNetworking.send(player, new TagPanelPayload(handler.syncId, view));
    handler.tags().sent(view);
  }

  private static boolean closed(ServerPlayerEntity player, int syncId) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || handler.syncId != syncId) {
      return false;
    }
    handler.tags().closed();
    return true;
  }
}
