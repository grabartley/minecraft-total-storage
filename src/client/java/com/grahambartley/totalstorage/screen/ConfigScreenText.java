package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Optional;
import net.minecraft.text.Text;

public final class ConfigScreenText {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".config.";

  private ConfigScreenText() {}

  public static Text title() {
    return line("title", "Total Storage settings");
  }

  public static Optional<Text> notice(ConfigScreenAccess.Notice notice) {
    return switch (notice) {
      case NONE -> Optional.empty();
      case NOT_CONNECTED ->
          Optional.of(
              line("not-connected", "Server settings show here while you are on a server."));
      case READ_ONLY ->
          Optional.of(
              line(
                  "read-only",
                  "These are this server's settings. Only an operator can change them."));
      case SAVE_FAILED ->
          Optional.of(
              line("save-failed", "Could not save your display options. The game log says why."));
    };
  }

  public static Text defaultValue(Text fallback) {
    return line("default", "Default %s", fallback);
  }

  static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
