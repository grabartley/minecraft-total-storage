package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

final class BooleanConfigSetting implements ConfigSetting {
  private static final List<String> VALUES = List.of("true", "false");

  private final String name;
  private final Predicate<TooManyChestsConfig> reader;
  private final BiFunction<ConfigBuilder, Boolean, ConfigBuilder> writer;
  private final boolean defaultValue;

  BooleanConfigSetting(
      String name,
      Predicate<TooManyChestsConfig> reader,
      BiFunction<ConfigBuilder, Boolean, ConfigBuilder> writer,
      boolean defaultValue) {
    this.name = Objects.requireNonNull(name, "name");
    this.reader = Objects.requireNonNull(reader, "reader");
    this.writer = Objects.requireNonNull(writer, "writer");
    this.defaultValue = defaultValue;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String valueIn(TooManyChestsConfig config) {
    return Boolean.toString(reader.test(config));
  }

  @Override
  public String defaultValue() {
    return Boolean.toString(defaultValue);
  }

  @Override
  public List<String> suggestions() {
    return VALUES;
  }

  @Override
  public String allowedValues() {
    return "true or false";
  }

  @Override
  public Text displayValue(String rawValue) {
    return ScreenTexts.onOrOff("true".equalsIgnoreCase(rawValue == null ? "" : rawValue.trim()));
  }

  @Override
  public Optional<TooManyChestsConfig> withValue(TooManyChestsConfig config, String rawValue) {
    if (config == null || rawValue == null) {
      return Optional.empty();
    }
    final String trimmed = rawValue.trim();
    if (!"true".equalsIgnoreCase(trimmed) && !"false".equalsIgnoreCase(trimmed)) {
      return Optional.empty();
    }
    return Optional.of(
        writer.apply(ConfigBuilder.from(config), Boolean.parseBoolean(trimmed)).build());
  }
}
