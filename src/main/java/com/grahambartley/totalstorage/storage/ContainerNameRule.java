package com.grahambartley.totalstorage.storage;

public final class ContainerNameRule {
  public static final int MAX_LENGTH = 32;

  private static final char FORMATTING = '\u00a7';

  private ContainerNameRule() {}

  public static String sanitise(String raw) {
    if (raw == null) {
      return "";
    }
    final StringBuilder kept = new StringBuilder(raw.length());
    boolean spacePending = false;
    for (int index = 0; index < raw.length(); index++) {
      final char character = raw.charAt(index);
      if (Character.isWhitespace(character)) {
        spacePending = !kept.isEmpty();
        continue;
      }
      if (Character.isISOControl(character) || character == FORMATTING) {
        continue;
      }
      if (spacePending) {
        kept.append(' ');
        spacePending = false;
      }
      kept.append(character);
    }
    return clip(kept.toString());
  }

  public static boolean isAcceptable(String raw) {
    return !sanitise(raw).isEmpty();
  }

  private static String clip(String collapsed) {
    if (collapsed.length() <= MAX_LENGTH) {
      return collapsed;
    }
    final int end =
        Character.isHighSurrogate(collapsed.charAt(MAX_LENGTH - 1)) ? MAX_LENGTH - 1 : MAX_LENGTH;
    return collapsed.substring(0, end).stripTrailing();
  }
}
