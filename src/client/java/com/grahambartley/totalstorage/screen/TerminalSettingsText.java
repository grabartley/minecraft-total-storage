package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import net.minecraft.text.Text;

public final class TerminalSettingsText {
  private TerminalSettingsText() {}

  public static Text trustedSummary(NetworkSettingsView view) {
    if (!view.answered()) {
      return TerminalScreenText.of("settings.pending");
    }
    if (!view.mayAdminister()) {
      return TerminalScreenText.of("settings.trusted.hidden.short");
    }
    return view.trusted().isEmpty()
        ? TerminalScreenText.of("settings.trusted.nobody")
        : Text.literal(String.join(", ", view.trusted()));
  }

  public static Text description(TerminalSetting setting, NetworkSettingsView view) {
    return switch (setting) {
      case ACCESS -> TerminalScreenText.of(setting.description());
      case TRUSTED ->
          view.answered() && view.accessMode() == TerminalAccessMode.TRUSTED
              ? TerminalScreenText.of("settings.trusted.description.active")
              : TerminalScreenText.of(setting.description());
      case RADIUS ->
          view.answered() && view.isClamped()
              ? TerminalScreenText.of("settings.radius.description.capped", view.effectiveRadius())
              : TerminalScreenText.of(setting.description());
    };
  }

  public static Text radiusValue(NetworkSettingsView view) {
    if (!view.answered()) {
      return Text.literal("-");
    }
    return TerminalScreenText.of(
        "settings.radius.value",
        view.isClamped() ? view.effectiveRadius() : view.radius(),
        view.maxRadius());
  }
}
