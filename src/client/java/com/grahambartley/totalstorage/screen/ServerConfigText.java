package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ConfigSection;
import com.grahambartley.totalstorage.config.ConfigSetting;
import net.minecraft.text.Text;

public final class ServerConfigText {
  private ServerConfigText() {}

  public static Text section(ConfigSection section) {
    return switch (section) {
      case DEFAULTS -> ConfigScreenText.line("section.defaults", "Defaults for new terminals");
      case SORTING -> ConfigScreenText.line("section.sorting", "Sorting");
      case FEATURES -> ConfigScreenText.line("section.features", "Features");
      case PERFORMANCE -> ConfigScreenText.line("section.performance", "Performance");
    };
  }

  public static Text label(ConfigSetting setting) {
    return ConfigScreenText.line("setting." + setting.name(), setting.name());
  }

  public static Text bounds(ConfigSetting setting) {
    final Text fallback = setting.displayValue(setting.defaultValue());
    return setting
        .range()
        .map(
            range ->
                ConfigScreenText.line(
                    "bounds",
                    "%s to %s, default %s",
                    setting.displayValue(Integer.toString(range.min())),
                    setting.displayValue(Integer.toString(range.max())),
                    fallback))
        .orElseGet(() -> ConfigScreenText.defaultValue(fallback));
  }
}
