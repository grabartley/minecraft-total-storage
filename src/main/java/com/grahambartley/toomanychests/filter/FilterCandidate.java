package com.grahambartley.toomanychests.filter;

import java.util.Objects;
import java.util.Set;

public record FilterCandidate(String identifier, String displayName, Set<String> tags) {
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

  public String modId() {
    final int separator = identifier.indexOf(FilterText.NAMESPACE_SEPARATOR);
    return separator < 0 ? DEFAULT_NAMESPACE : identifier.substring(0, separator);
  }

  public String path() {
    final int separator = identifier.indexOf(FilterText.NAMESPACE_SEPARATOR);
    return separator < 0 ? identifier : identifier.substring(separator + 1);
  }

  public boolean hasTag(String tag) {
    return tags.contains(FilterText.normalise(tag));
  }
}
