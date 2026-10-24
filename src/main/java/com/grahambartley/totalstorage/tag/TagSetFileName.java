package com.grahambartley.totalstorage.tag;

import java.util.Locale;
import java.util.Optional;

public final class TagSetFileName {
  public static final int MAX_LENGTH = 32;

  private TagSetFileName() {}

  public static Optional<String> of(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    final String trimmed = raw.trim().toLowerCase(Locale.ROOT);
    if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH || !isWritable(trimmed)) {
      return Optional.empty();
    }
    return Optional.of(trimmed);
  }

  private static boolean isWritable(String name) {
    for (int index = 0; index < name.length(); index++) {
      final char character = name.charAt(index);
      final boolean allowed =
          character == '_'
              || character == '-'
              || (character >= '0' && character <= '9')
              || (character >= 'a' && character <= 'z');
      if (!allowed) {
        return false;
      }
    }
    return true;
  }
}
