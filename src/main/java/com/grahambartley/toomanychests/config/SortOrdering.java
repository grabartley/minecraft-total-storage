package com.grahambartley.toomanychests.config;

import java.util.Locale;
import java.util.Optional;

/** Which ordering rule sorting follows. See ADR 0005. */
public enum SortOrdering {
  REGISTRY_ID,
  DISPLAY_NAME;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<SortOrdering> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (SortOrdering ordering : values()) {
      if (ordering.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(ordering);
      }
    }
    return Optional.empty();
  }
}
