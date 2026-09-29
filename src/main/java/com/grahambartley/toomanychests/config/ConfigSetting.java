package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;

public interface ConfigSetting {
  String name();

  String valueIn(TooManyChestsConfig config);

  String defaultValue();

  List<String> suggestions();

  String allowedValues();

  Text displayValue(String rawValue);

  default Optional<ConfigRange> range() {
    return Optional.empty();
  }

  Optional<TooManyChestsConfig> withValue(TooManyChestsConfig config, String rawValue);

  default TooManyChestsConfig reset(TooManyChestsConfig config) {
    return withValue(config, defaultValue()).orElse(config);
  }
}
