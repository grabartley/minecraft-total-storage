package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

public record TerminalCaller(Optional<UUID> player, boolean operator) {
  private static final TerminalCaller NOBODY = new TerminalCaller(Optional.empty(), false);

  public TerminalCaller {
    player = player == null ? Optional.empty() : player;
  }

  public static TerminalCaller of(PlayerEntity asking) {
    if (asking == null) {
      return NOBODY;
    }
    return new TerminalCaller(
        Optional.of(asking.getUuid()), TerminalAccessService.isOperator(asking));
  }

  public static TerminalCaller of(PlayerEntity asking, boolean operator) {
    return asking == null
        ? new TerminalCaller(Optional.empty(), operator)
        : new TerminalCaller(Optional.of(asking.getUuid()), operator);
  }

  public boolean mayAdminister(TerminalSettings settings) {
    return TerminalAccessRules.mayAdminister(settings, player.orElse(null), operator);
  }
}
