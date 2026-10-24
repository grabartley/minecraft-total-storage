package com.grahambartley.totalstorage.access;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.UUID;

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

  public static boolean mayAdminister(TerminalSettings settings, UUID player, boolean operator) {
    if (settings == null) {
      return false;
    }
    return operator || settings.isOwnedBy(player);
  }
}
