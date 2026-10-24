package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.ConfigSection;
import com.grahambartley.totalstorage.config.ConfigSetting;
import net.minecraft.text.Text;

public final class ServerConfigText {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".config.";

  private ServerConfigText() {}

  public static Text title() {
    return line("title", "Total Storage server settings");
  }

  public static Text readOnlyNotice() {
    return line("read-only", "These are this server's settings. Only an operator can change them.");
  }

  public static Text notConnectedNotice() {
    return line(
        "not-connected",
        "These are a server's settings, so they are only readable while you are on one.");
  }

  public static Text section(ConfigSection section) {
    return switch (section) {
      case DEFAULTS -> line("section.defaults", "Defaults for new terminals");
      case SORTING -> line("section.sorting", "Sorting");
      case FEATURES -> line("section.features", "Features");
      case PERFORMANCE -> line("section.performance", "Performance");
    };
  }

  public static Text label(ConfigSetting setting) {
    return line("setting." + setting.name(), setting.name());
  }

  public static Text bounds(ConfigSetting setting) {
    final Text fallback = setting.displayValue(setting.defaultValue());
    return setting
        .range()
        .map(
            range ->
                line(
                    "bounds",
                    "%s to %s, default %s",
                    setting.displayValue(Integer.toString(range.min())),
                    setting.displayValue(Integer.toString(range.max())),
                    fallback))
        .orElseGet(() -> line("default", "Default %s", fallback));
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
