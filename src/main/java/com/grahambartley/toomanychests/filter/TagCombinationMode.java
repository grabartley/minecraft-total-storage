package com.grahambartley.toomanychests.filter;

import java.util.Optional;
import java.util.Set;

/**
 * How several active tag filters combine into one decision.
 *
 * <p>An empty filter set matches everything under either mode, because no filters selected means no
 * narrowing rather than nothing to show.
 */
public enum TagCombinationMode {
  /** Every active tag must be carried, so each extra filter narrows the view. */
  AND {
    @Override
    boolean combine(Set<String> itemTags, Set<String> activeTags) {
      return itemTags.containsAll(activeTags);
    }
  },
  /** At least one active tag must be carried, so each extra filter widens the view. */
  OR {
    @Override
    boolean combine(Set<String> itemTags, Set<String> activeTags) {
      for (final String activeTag : activeTags) {
        if (itemTags.contains(activeTag)) {
          return true;
        }
      }
      return false;
    }
  };

  public static final TagCombinationMode DEFAULT = AND;

  /**
   * The mode of that name, or none. Modes travel by name rather than by position, so reordering the
   * constants cannot quietly turn one mode into the other for a build on the far side of a wire.
   */
  public static Optional<TagCombinationMode> fromName(String name) {
    if (name == null) {
      return Optional.empty();
    }
    for (final TagCombinationMode candidate : values()) {
      if (candidate.name().equalsIgnoreCase(name.trim())) {
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }

  abstract boolean combine(Set<String> itemTags, Set<String> activeTags);

  /**
   * @param itemTags the tags the item is filed under, normalised
   * @param activeTags the tags the player has selected, normalised
   */
  public boolean matches(Set<String> itemTags, Set<String> activeTags) {
    if (activeTags == null || activeTags.isEmpty()) {
      return true;
    }
    return itemTags != null && !itemTags.isEmpty() && combine(itemTags, activeTags);
  }
}
