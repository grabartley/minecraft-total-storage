package com.grahambartley.totalstorage.config;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;

public interface ConfigSetting {
  String name();

  String valueIn(TotalStorageConfig config);

  String defaultValue();

  List<String> suggestions();

  String allowedValues();

  Text displayValue(String rawValue);

  default Optional<ConfigRange> range() {
    return Optional.empty();
  }

  Optional<TotalStorageConfig> withValue(TotalStorageConfig config, String rawValue);

  default TotalStorageConfig reset(TotalStorageConfig config) {
    return withValue(config, defaultValue()).orElse(config);
  }
}
