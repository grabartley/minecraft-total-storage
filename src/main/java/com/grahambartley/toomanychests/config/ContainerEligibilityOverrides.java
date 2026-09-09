package com.grahambartley.toomanychests.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class ContainerEligibilityOverrides {
  private ContainerEligibilityOverrides() {}

  public static Optional<TooManyChestsConfig> set(
      TooManyChestsConfig config, String blockId, boolean eligible) {
    return parse(config, blockId)
        .map(
            id -> {
              final Map<String, Boolean> edited = editable(config);
              edited.put(id.toString(), eligible);
              return config.withContainerEligibilityOverrides(edited);
            });
  }

  public static Optional<TooManyChestsConfig> clear(TooManyChestsConfig config, String blockId) {
    return parse(config, blockId)
        .flatMap(
            id -> {
              final Map<String, Boolean> edited = editable(config);
              return edited.remove(id.toString()) == null
                  ? Optional.empty()
                  : Optional.of(config.withContainerEligibilityOverrides(edited));
            });
  }

  public static TooManyChestsConfig clearAll(TooManyChestsConfig config) {
    return config == null ? null : config.withContainerEligibilityOverrides(Map.of());
  }

  private static Optional<Identifier> parse(TooManyChestsConfig config, String blockId) {
    if (config == null || blockId == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(Identifier.tryParse(blockId.trim()))
        .filter(id -> !id.getPath().isBlank());
  }

  private static Map<String, Boolean> editable(TooManyChestsConfig config) {
    return new LinkedHashMap<>(config.containerEligibilityOverrides());
  }
}
