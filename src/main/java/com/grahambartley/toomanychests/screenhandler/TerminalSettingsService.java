package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.network.NetworkSettingsPayload;
import com.grahambartley.toomanychests.network.NetworkSettingsRequestPayload;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.terminal.NetworkSettingsService;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Answers a settings panel's request for the terminal it is drawing, and builds it again on demand.
 *
 * <p>The same shape as {@link TerminalListingService} and for the same reasons: every answer is
 * built from the terminal as it stands rather than from anything the client sent back, the sync id
 * is checked against the handler the server already holds, and a panel that has asked for nothing
 * is never pushed to.
 *
 * <p>Deciding when a panel is owed a fresh answer is not this class's job. An edit pushes to
 * everyone looking, which is {@link TerminalSettingsEdits}, because an edit is the only thing that
 * changes what a panel shows.
 */
public final class TerminalSettingsService {
  private final NetworkSettingsService settings;

  public TerminalSettingsService() {
    this(new NetworkSettingsService());
  }

  public TerminalSettingsService(NetworkSettingsService settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  /** Records what the panel asked for and answers it, or drops a request naming another screen. */
  public boolean request(ServerPlayerEntity player, NetworkSettingsRequestPayload payload) {
    if (payload == null) {
      return false;
    }
    if (payload.window().isEmpty()) {
      return closed(player, payload.syncId());
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    context.handler().settings().requested(payload.window());
    send(player, context.handler());
    return true;
  }

  /**
   * A panel saying it has stopped drawing, which is a screen going back to the item grid.
   *
   * <p>Resolved without the gate on purpose. The gate needs a live network, and a terminal that
   * lost power under an open screen is exactly when this has to land: refusing it would leave the
   * server pushing panels at a screen that is not showing one. Nothing is read or written but the
   * screen's own record of what it wants, so there is no permission left to check.
   *
   * <p>No answer is sent. The panel that asked has stopped drawing, and replying would overwrite
   * its mirror with a view it is about to throw away.
   */
  private static boolean closed(ServerPlayerEntity player, int syncId) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || handler.syncId != syncId) {
      return false;
    }
    handler.settings().closed();
    return true;
  }

  /** Sends the page the panel is already asking for, which is how an edit reaches a bystander. */
  public boolean refresh(ServerPlayerEntity player) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !handler.settings().isOpen()) {
      return false;
    }
    send(player, handler);
    return true;
  }

  /**
   * The panel this screen is currently asking for. Separate from sending it so it can be read
   * directly, which is what lets the panel be proven against a real terminal rather than a packet.
   */
  public NetworkSettingsView viewFor(ServerPlayerEntity player, TerminalScreenHandler handler) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(handler, "handler");
    return settings.viewFor(player, handler.terminal(), handler.settings().window());
  }

  private void send(ServerPlayerEntity player, TerminalScreenHandler handler) {
    ServerPlayNetworking.send(
        player, new NetworkSettingsPayload(handler.syncId, viewFor(player, handler)));
  }
}
