package com.grahambartley.toomanychests.filter;

import java.util.Objects;
import java.util.Set;

/**
 * Everything the filter engine needs to know about one item, as plain data.
 *
 * <p>Per ADR 0012 this is what keeps evaluation testable: the game side flattens an item variant
 * into these three strings once, and nothing downstream needs a registry to reason about a match.
 *
 * @param identifier the namespaced item identifier, such as {@code minecraft:cobblestone}
 * @param displayName the item's translated name, which is what a player is usually typing
 * @param tags the tags the item is filed under, preset and custom alike
 */
public record FilterCandidate(String identifier, String displayName, Set<String> tags) {
  private static final char NAMESPACE_SEPARATOR = ':';
  private static final String DEFAULT_NAMESPACE = "minecraft";

  public FilterCandidate {
    Objects.requireNonNull(identifier, "identifier");
    identifier = FilterText.normalise(identifier);
    if (identifier.isEmpty()) {
      throw new IllegalArgumentException("identifier must not be blank");
    }
    displayName = FilterText.normalise(displayName);
    tags = FilterText.normaliseAll(tags);
  }

  /** The mod the item came from, which is what the {@code @} search prefix filters on. */
  public String modId() {
    final int separator = identifier.indexOf(NAMESPACE_SEPARATOR);
    return separator < 0 ? DEFAULT_NAMESPACE : identifier.substring(0, separator);
  }

  /** The identifier without its namespace, so a bare name search still finds vanilla items. */
  public String path() {
    final int separator = identifier.indexOf(NAMESPACE_SEPARATOR);
    return separator < 0 ? identifier : identifier.substring(separator + 1);
  }

  public boolean hasTag(String tag) {
    return tags.contains(FilterText.normalise(tag));
  }
}
