package com.grahambartley.toomanychests.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class FilterText {
  public static final char NAMESPACE_SEPARATOR = ':';

  private FilterText() {}

  public static boolean hasNamespace(String value) {
    return value != null && value.indexOf(NAMESPACE_SEPARATOR) >= 0;
  }

  public static String normalise(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  public static boolean isBlank(String value) {
    return normalise(value).isEmpty();
  }

  public static Set<String> normaliseAll(Collection<String> values) {
    if (values == null || values.isEmpty()) {
      return Set.of();
    }
    final Set<String> normalised = new LinkedHashSet<>(values.size());
    for (final String value : values) {
      final String candidate = normalise(value);
      if (!candidate.isEmpty()) {
        normalised.add(candidate);
      }
    }
    return Collections.unmodifiableSet(normalised);
  }
}
