package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.text.Text;

final class EnumConfigSetting<E extends Enum<E> & ConfigChoice> implements ConfigSetting {
  private final String name;
  private final List<E> alternatives;
  private final Function<TooManyChestsConfig, E> reader;
  private final BiFunction<ConfigBuilder, E, ConfigBuilder> writer;
  private final E defaultValue;

  EnumConfigSetting(
      String name,
      Class<E> type,
      Function<TooManyChestsConfig, E> reader,
      BiFunction<ConfigBuilder, E, ConfigBuilder> writer,
      E defaultValue) {
    this.name = Objects.requireNonNull(name, "name");
    this.alternatives = List.of(Objects.requireNonNull(type, "type").getEnumConstants());
    this.reader = Objects.requireNonNull(reader, "reader");
    this.writer = Objects.requireNonNull(writer, "writer");
    this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String valueIn(TooManyChestsConfig config) {
    return reader.apply(config).id();
  }

  @Override
  public String defaultValue() {
    return defaultValue.id();
  }

  @Override
  public List<String> suggestions() {
    return alternatives.stream().map(ConfigEnum::id).toList();
  }

  @Override
  public String allowedValues() {
    return String.join(", ", suggestions());
  }

  @Override
  public Text displayValue(String rawValue) {
    final String trimmed = rawValue == null ? "" : rawValue.trim();
    return alternatives.stream()
        .filter(alternative -> alternative.id().equalsIgnoreCase(trimmed))
        .findFirst()
        .map(ConfigChoice::displayName)
        .orElseGet(() -> Text.literal(trimmed));
  }

  @Override
  public Optional<TooManyChestsConfig> withValue(TooManyChestsConfig config, String rawValue) {
    if (config == null || rawValue == null) {
      return Optional.empty();
    }
    final String trimmed = rawValue.trim();
    return alternatives.stream()
        .filter(alternative -> alternative.id().equalsIgnoreCase(trimmed))
        .findFirst()
        .map(alternative -> writer.apply(ConfigBuilder.from(config), alternative).build());
  }
}
