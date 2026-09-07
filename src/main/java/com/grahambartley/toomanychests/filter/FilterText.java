package com.grahambartley.toomanychests.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The one place text entering the filter engine is folded to a comparable form.
 *
 * <p>Tag identifiers, mod identifiers, search terms, and display names all arrive from different
 * places with different casing. Normalising at every boundary rather than at comparison time keeps
 * the matcher itself free of case handling.
 */
public final class FilterText {
  /** What separates a namespace from a path, and what marks a term as naming one outright. */
  public static final char NAMESPACE_SEPARATOR = ':';

  private FilterText() {}

  /**
   * Whether a term names a namespace, which per ADR 0013 is matched literally rather than fuzzily.
   */
  public static boolean hasNamespace(String value) {
    return value != null && value.indexOf(NAMESPACE_SEPARATOR) >= 0;
  }

  /** Lower cased and trimmed, with null and blank collapsing to the empty string. */
  public static String normalise(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  public static boolean isBlank(String value) {
    return normalise(value).isEmpty();
  }

  /** Normalises every entry, dropping blanks, preserving encounter order. */
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
