package com.grahambartley.toomanychests.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

/**
 * Edits to the operator's forced verdicts on which blocks join a network.
 *
 * <p>Kept apart from the settings list because these are not one value but a growing set keyed by
 * block, and forcing them through a single {@code set name value} shape would mean a setting whose
 * name changes with the world's block list.
 *
 * <p>An edit naming a block id that does not parse is refused rather than stored, since a stored
 * unparseable key is one that can never match a real block and never be found again to remove.
 */
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

  /** Empty when the id is unusable or nothing was overriding that block in the first place. */
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

  /**
   * An empty path parses as a valid identifier, so it is refused here rather than stored as a
   * namespace with nothing after it that no block will ever answer to.
   */
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
