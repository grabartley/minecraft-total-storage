package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.ConfigSection;
import com.grahambartley.toomanychests.config.ConfigSetting;
import net.minecraft.text.Text;

public final class ServerConfigText {
  private static final String PREFIX = "screen." + TooManyChests.MOD_ID + ".config.";

  private ServerConfigText() {}

  public static Text title() {
    return line("title", "Too Many Chests server settings");
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
    return line("bounds", "%s, default %s", setting.allowedValues(), setting.defaultValue());
  }

  public static Text value(String rawValue) {
    return Text.literal(rawValue);
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
