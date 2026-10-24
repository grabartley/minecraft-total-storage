package com.grahambartley.totalstorage.filter;

import java.util.Optional;
import java.util.Set;

public enum TagCombinationMode {
  AND {
    @Override
    boolean combine(Set<String> itemTags, Set<String> activeTags) {
      return itemTags.containsAll(activeTags);
    }
  },

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

  public boolean matches(Set<String> itemTags, Set<String> activeTags) {
    if (activeTags == null || activeTags.isEmpty()) {
      return true;
    }
    return itemTags != null && !itemTags.isEmpty() && combine(itemTags, activeTags);
  }
}
