package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.ModNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Puts a screen back in step with the server after an operation that did not go as it expected.
 *
 * <p>Per ADR 0009 a refused request is an ordinary outcome, not an error, and the client that sent
 * it is by definition looking at something the server disagrees with. Answering with silence would
 * leave that disagreement on screen until the player scrolled, so every refusal ends here instead.
 *
 * <p>Both halves are resent because a request can be wrong about either: the listing, when the
 * network no longer holds what the grid is showing, and the handler's own state, when the cursor or
 * a player slot moved.
 */
public final class TerminalResync {

  private TerminalResync() {}

  public static void of(ServerPlayerEntity player) {
    if (player == null) {
      return;
    }
    ModNetworking.listings().refresh(player);
    if (player.currentScreenHandler != null) {
      player.currentScreenHandler.syncState();
    }
  }
}
