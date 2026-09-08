package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Whoever is asking a terminal to change, reduced to the two facts that decide whether it may.
 *
 * <p>The same question arrives from a screen and from a command line, and the two know different
 * things: a screen has a player entity, and a console has a permission level and nobody behind it.
 * Reducing both to this is what lets ADR 0008's rule be applied once rather than reimplemented per
 * entry point, which is exactly the drift {@code /tmc terminal} exists to avoid.
 *
 * <p>A caller with no player is a console or a command block. It has no ownership to appeal to, so
 * it is held to the operator level alone.
 *
 * @param player who is asking, empty for a caller that is not a player
 * @param operator whether they hold the level the moderation bypass is granted at
 */
public record TerminalCaller(Optional<UUID> player, boolean operator) {
  private static final TerminalCaller NOBODY = new TerminalCaller(Optional.empty(), false);

  public TerminalCaller {
    player = player == null ? Optional.empty() : player;
  }

  /** A player asking through a screen, whose operator level is read from the server they are on. */
  public static TerminalCaller of(PlayerEntity asking) {
    if (asking == null) {
      return NOBODY;
    }
    return new TerminalCaller(
        Optional.of(asking.getUuid()), TerminalAccessService.isOperator(asking));
  }

  /** A caller whose player and level are both already known, which a command source is. */
  public static TerminalCaller of(PlayerEntity asking, boolean operator) {
    return asking == null
        ? new TerminalCaller(Optional.empty(), operator)
        : new TerminalCaller(Optional.of(asking.getUuid()), operator);
  }

  /** Whether this caller may move a terminal's settings, per ADR 0008. */
  public boolean mayAdminister(TerminalSettings settings) {
    return TerminalAccessRules.mayAdminister(settings, player.orElse(null), operator);
  }
}
