package com.grahambartley.toomanychests.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class FilterText {
  public static final char NAMESPACE_SEPARATOR = ':';

  public static final String TAG_SPACE_REPLACEMENT = "-";

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private FilterText() {}

  public static String tagId(String value) {
    final String normalised = normalise(value);
    return carriesWhitespace(normalised)
        ? WHITESPACE.matcher(normalised).replaceAll(TAG_SPACE_REPLACEMENT)
        : normalised;
  }

  public static Set<String> tagIds(Collection<String> values) {
    return canonicalise(values, FilterText::tagId);
  }

  private static boolean carriesWhitespace(String value) {
    for (int index = 0; index < value.length(); index++) {
      if (Character.isWhitespace(value.charAt(index))) {
        return true;
      }
    }
    return false;
  }

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
    return canonicalise(values, FilterText::normalise);
  }

  private static Set<String> canonicalise(Collection<String> values, UnaryOperator<String> rule) {
    if (values == null || values.isEmpty()) {
      return Set.of();
    }
    final Set<String> canonical = new LinkedHashSet<>(values.size());
    for (final String value : values) {
      final String candidate = rule.apply(value);
      if (!candidate.isEmpty()) {
        canonical.add(candidate);
      }
    }
    return Collections.unmodifiableSet(canonical);
  }
}
