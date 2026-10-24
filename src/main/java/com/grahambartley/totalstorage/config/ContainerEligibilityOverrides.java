package com.grahambartley.totalstorage.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class ContainerEligibilityOverrides {
  private ContainerEligibilityOverrides() {}

  public static Optional<TotalStorageConfig> set(
      TotalStorageConfig config, String blockId, boolean eligible) {
    return parse(config, blockId)
        .map(
            id -> {
              final Map<String, Boolean> edited = editable(config);
              edited.put(id.toString(), eligible);
              return config.withContainerEligibilityOverrides(edited);
            });
  }

  public static Optional<TotalStorageConfig> clear(TotalStorageConfig config, String blockId) {
    return parse(config, blockId)
        .flatMap(
            id -> {
              final Map<String, Boolean> edited = editable(config);
              return edited.remove(id.toString()) == null
                  ? Optional.empty()
                  : Optional.of(config.withContainerEligibilityOverrides(edited));
            });
  }

  public static TotalStorageConfig clearAll(TotalStorageConfig config) {
    return config == null ? null : config.withContainerEligibilityOverrides(Map.of());
  }

  private static Optional<Identifier> parse(TotalStorageConfig config, String blockId) {
    if (config == null || blockId == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(Identifier.tryParse(blockId.trim()))
        .filter(id -> !id.getPath().isBlank());
  }

  private static Map<String, Boolean> editable(TotalStorageConfig config) {
    return new LinkedHashMap<>(config.containerEligibilityOverrides());
  }
}
