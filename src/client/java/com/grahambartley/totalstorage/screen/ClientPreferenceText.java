package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public final class ClientPreferenceText {
  private ClientPreferenceText() {}

  public static Text section() {
    return ConfigScreenText.line("section.display", "Your display options");
  }

  public static Text label(ClientPreference preference) {
    final String key = "preference." + preference.key();
    return switch (preference) {
      case SORT_BUTTON_VISIBLE -> ConfigScreenText.line(key, "Show the sort button");
      case SCREEN_BUTTON_ANCHOR -> ConfigScreenText.line(key, "Sort and stack buttons");
      case LOCKED_SLOT_TINT_VISIBLE -> ConfigScreenText.line(key, "Tint locked slots");
    };
  }

  public static Text displayValue(ClientPreference preference, String rawValue) {
    if (preference == ClientPreference.SCREEN_BUTTON_ANCHOR) {
      return ScreenButtonAnchor.fromId(rawValue)
          .map(ClientPreferenceText::anchor)
          .orElseGet(() -> Text.literal(String.valueOf(rawValue)));
    }
    return ScreenTexts.onOrOff(Boolean.parseBoolean(rawValue));
  }

  public static Text defaultLine(ClientPreference preference) {
    return ConfigScreenText.defaultValue(displayValue(preference, preference.defaultValue()));
  }

  private static Text anchor(ScreenButtonAnchor anchor) {
    final String key = "value.anchor." + anchor.id();
    return switch (anchor) {
      case INSIDE_TOP_RIGHT -> ConfigScreenText.line(key, "Inside top");
      case OUTSIDE_TOP_RIGHT -> ConfigScreenText.line(key, "Outside top");
      case OUTSIDE_BOTTOM_RIGHT -> ConfigScreenText.line(key, "Outside bottom");
    };
  }
}
