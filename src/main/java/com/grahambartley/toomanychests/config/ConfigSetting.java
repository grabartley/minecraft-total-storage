package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Optional;

/**
 * One server setting, described once so the command tree does not have to know it exists.
 *
 * <p>A literal Brigadier node per setting means a new setting is only reachable from the command
 * line once somebody remembers to add it there, and the compiler never asks. Describing settings
 * instead of hard-coding them makes {@code /tmc config list} and the command's tab completion read
 * from the same list the config record does, so a setting cannot be added and left unreachable.
 *
 * <p>An unusable value is refused rather than clamped. Loading a hand-edited file clamps, because a
 * bad edit must never stop a server starting, but an operator typing a value at a console is
 * present to be told they got it wrong.
 */
public interface ConfigSetting {
  String name();

  /** How this setting's value reads in the given configuration. */
  String valueIn(TooManyChestsConfig config);

  String defaultValue();

  /** Values worth offering at a prompt, which for a range is its ends rather than every step. */
  List<String> suggestions();

  /** What an operator who typed something unusable needs to be told they may type instead. */
  String allowedValues();

  /** The configuration with this setting moved, or empty when the value is not one this takes. */
  Optional<TooManyChestsConfig> withValue(TooManyChestsConfig config, String rawValue);

  default TooManyChestsConfig reset(TooManyChestsConfig config) {
    return withValue(config, defaultValue()).orElse(config);
  }
}
