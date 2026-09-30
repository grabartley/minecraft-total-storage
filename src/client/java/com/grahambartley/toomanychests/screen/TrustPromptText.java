package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.access.TrustEditOutcome;
import net.minecraft.text.Text;

public final class TrustPromptText {
  private TrustPromptText() {}

  public static Text refusal(TrustEditOutcome outcome, String player) {
    return switch (outcome) {
      case UNKNOWN_PLAYER -> Text.translatable("argument.player.unknown");
      case ALREADY_TRUSTED -> TerminalScreenText.of("settings.trusted.already", player);
      case NOT_TRUSTED -> TerminalScreenText.of("settings.trusted.missing", player);
      case DENIED -> TerminalScreenText.of("settings.trusted.hidden");
      case ADDED, REMOVED -> Text.empty();
    };
  }
}
