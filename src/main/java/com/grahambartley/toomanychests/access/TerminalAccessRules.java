package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.UUID;

/**
 * Who a terminal lets in, decided from its settings alone. See ADR 0008.
 *
 * <p>Kept free of Minecraft so the one rule that stands between a player and every container in a
 * base can be tested exhaustively rather than in a running world.
 *
 * <p>A terminal nobody owns is a terminal nobody can be locked out of. Placement records an owner,
 * so an unowned terminal came from a command or a dispenser, and treating it as owner-locked would
 * make it permanently unusable rather than secure. Configuring one is still operators only, so
 * being able to use it is not a way to claim it.
 */
public final class TerminalAccessRules {

  private TerminalAccessRules() {}

  public static TerminalAccess decide(TerminalSettings settings, UUID player, boolean operator) {
    if (settings == null || player == null) {
      return TerminalAccess.DENIED;
    }
    if (settings.accessMode() == TerminalAccessMode.OPEN) {
      return TerminalAccess.OPEN;
    }
    if (settings.isOwnedBy(player)) {
      return TerminalAccess.OWNER;
    }
    if (settings.owner().isEmpty()) {
      return TerminalAccess.UNOWNED;
    }
    if (settings.accessMode() == TerminalAccessMode.TRUSTED && settings.isTrusted(player)) {
      return TerminalAccess.TRUSTED;
    }
    return operator ? TerminalAccess.OPERATOR : TerminalAccess.DENIED;
  }

  /**
   * Whether a player may move the access mode or edit the trust list, which is a narrower question
   * than whether they may use the terminal: an open terminal lets everybody in without letting
   * anybody lock it again.
   */
  public static boolean mayAdminister(TerminalSettings settings, UUID player, boolean operator) {
    if (settings == null) {
      return false;
    }
    return operator || settings.isOwnedBy(player);
  }
}
