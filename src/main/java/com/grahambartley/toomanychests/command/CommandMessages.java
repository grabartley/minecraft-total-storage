package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ConfigSetting;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.minecraft.text.Text;

/**
 * Everything the command tree says, in one place.
 *
 * <p>Each line carries its English alongside its translation key. A dedicated server's console
 * holds no resource pack, so a plain translatable line reaches the operator who most needs it as a
 * raw key, and that operator is the whole reason these commands exist.
 */
public final class CommandMessages {
  private static final String PREFIX = "command.too-many-chests.";

  private CommandMessages() {}

  public static Text helpHeader() {
    return line("help.header", "Too Many Chests commands:");
  }

  public static Text helpConfig() {
    return line(
        "help.config",
        "/tmc config list | get <setting> | set <setting> <value> | reset <setting> | reset all");
  }

  public static Text helpEligibility() {
    return line(
        "help.eligibility",
        "/tmc config eligibility list | set <block> <true|false> | clear <block> | clear all");
  }

  public static Text helpSort() {
    return line("help.sort", "/tmc sort sorts the container you have open");
  }

  public static Text configHeader() {
    return line("config.header", "Too Many Chests settings:");
  }

  public static Text settingLine(ConfigSetting setting, TooManyChestsConfig config) {
    return line(
        "config.entry",
        "%s = %s (default %s)",
        setting.name(),
        setting.valueIn(config),
        setting.defaultValue());
  }

  public static Text settingValue(ConfigSetting setting, TooManyChestsConfig config) {
    return line("config.value", "%s = %s", setting.name(), setting.valueIn(config));
  }

  public static Text settingSet(ConfigSetting setting, String value) {
    return line("config.set", "Set %s to %s", setting.name(), value);
  }

  public static Text settingReset(ConfigSetting setting, String value) {
    return line("config.reset", "Reset %s to its default of %s", setting.name(), value);
  }

  public static Text everySettingReset() {
    return line("config.reset.all", "Reset every Too Many Chests setting to its default");
  }

  public static Text unknownSetting(String name, String known) {
    return line("config.unknown", "Unknown setting '%s'. Settings are: %s", name, known);
  }

  public static Text invalidValue(ConfigSetting setting, String value) {
    return line(
        "config.invalid",
        "'%s' is not a value %s takes. Expected %s",
        value,
        setting.name(),
        setting.allowedValues());
  }

  public static Text updateFailed() {
    return line(
        "config.failed", "Could not save the Too Many Chests config, see the server log for why");
  }

  public static Text eligibilityHeader() {
    return line("eligibility.header", "Container eligibility overrides:");
  }

  public static Text eligibilityEmpty() {
    return line(
        "eligibility.empty", "No container eligibility overrides are set, so the rules decide");
  }

  public static Text eligibilityLine(String blockId, boolean eligible) {
    return line(
        "eligibility.entry", "%s is forced %s", blockId, eligible ? "eligible" : "ineligible");
  }

  public static Text eligibilitySet(String blockId, boolean eligible) {
    return line("eligibility.set", "Forced %s %s", blockId, eligible ? "eligible" : "ineligible");
  }

  public static Text eligibilityCleared(String blockId) {
    return line("eligibility.cleared", "Stopped forcing eligibility for %s", blockId);
  }

  public static Text eligibilityUnusableBlock(String blockId) {
    return line("eligibility.unusable", "'%s' does not name a block", blockId);
  }

  public static Text eligibilityNotOverridden(String blockId) {
    return line("eligibility.missing", "Nothing was forcing eligibility for %s", blockId);
  }

  public static Text everyEligibilityCleared() {
    return line("eligibility.cleared.all", "Cleared every container eligibility override");
  }

  public static Text sorted() {
    return line("sort.sorted", "Sorted");
  }

  public static Text nothingToSort() {
    return line("sort.empty", "There is nothing in there to sort");
  }

  public static Text nothingOpenToSort() {
    return line("sort.refused", "That is not something this mod will sort");
  }

  public static Text sortFailed() {
    return line("sort.failed", "The sort was rolled back and nothing moved");
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
