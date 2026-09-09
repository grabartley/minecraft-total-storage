package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

final class BoundedIntConfigSetting implements ConfigSetting {
  private final String name;
  private final int min;
  private final int max;
  private final int defaultValue;
  private final ToIntFunction<TooManyChestsConfig> reader;
  private final BiFunction<ConfigBuilder, Integer, ConfigBuilder> writer;

  BoundedIntConfigSetting(
      String name,
      int min,
      int max,
      int defaultValue,
      ToIntFunction<TooManyChestsConfig> reader,
      BiFunction<ConfigBuilder, Integer, ConfigBuilder> writer) {
    this.name = Objects.requireNonNull(name, "name");
    this.min = min;
    this.max = max;
    this.defaultValue = defaultValue;
    this.reader = Objects.requireNonNull(reader, "reader");
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String valueIn(TooManyChestsConfig config) {
    return Integer.toString(reader.applyAsInt(config));
  }

  @Override
  public String defaultValue() {
    return Integer.toString(defaultValue);
  }

  @Override
  public List<String> suggestions() {
    return Stream.of(min, defaultValue, max).distinct().sorted().map(String::valueOf).toList();
  }

  @Override
  public String allowedValues() {
    return "a whole number from " + min + " to " + max;
  }

  @Override
  public Optional<TooManyChestsConfig> withValue(TooManyChestsConfig config, String rawValue) {
    if (config == null || rawValue == null) {
      return Optional.empty();
    }
    final int value;
    try {
      value = Integer.parseInt(rawValue.trim());
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
    if (value < min || value > max) {
      return Optional.empty();
    }
    return Optional.of(writer.apply(ConfigBuilder.from(config), value).build());
  }
}
