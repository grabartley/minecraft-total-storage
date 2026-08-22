package com.grahambartley.toomanychests.filter;

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
