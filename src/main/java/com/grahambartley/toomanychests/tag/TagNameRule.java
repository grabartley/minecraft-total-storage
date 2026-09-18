package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;

public final class TagNameRule {
  public static final int MAX_LENGTH = 32;

  public static final String SPACE_REPLACEMENT = "-";

  private TagNameRule() {}

  public static String idFor(String name) {
    return FilterText.normalise(name).replaceAll("\\s+", SPACE_REPLACEMENT);
  }

  public static TagNameOutcome check(String name, NetworkTagSet existing) {
    return check(name, existing, "");
  }

  public static TagNameOutcome check(String name, NetworkTagSet existing, String replacing) {
    final String id = idFor(name);
    if (id.isEmpty()) {
      return TagNameOutcome.BLANK;
    }
    if (id.length() > MAX_LENGTH) {
      return TagNameOutcome.TOO_LONG;
    }
    if (!isWritable(id)) {
      return TagNameOutcome.ILLEGAL_CHARACTER;
    }
    if (PresetTag.fromId(id).isPresent()) {
      return TagNameOutcome.PRESET_COLLISION;
    }
    if (existing != null && existing.contains(id) && !id.equals(idFor(replacing))) {
      return TagNameOutcome.DUPLICATE;
    }
    return TagNameOutcome.OK;
  }

  private static boolean isWritable(String id) {
    for (int index = 0; index < id.length(); index++) {
      final char character = id.charAt(index);
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
