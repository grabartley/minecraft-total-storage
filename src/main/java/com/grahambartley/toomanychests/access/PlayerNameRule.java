package com.grahambartley.toomanychests.access;

public final class PlayerNameRule {
  public static final int MAX_TYPED_LENGTH = 16;

  public static final int MAX_SHOWN_LENGTH = 36;

  private PlayerNameRule() {}

  public static boolean isNameCharacter(char character) {
    return character == '_'
        || (character >= '0' && character <= '9')
        || (character >= 'a' && character <= 'z')
        || (character >= 'A' && character <= 'Z');
  }

  public static String clamp(String name) {
    if (name == null) {
      return "";
    }
    final String kept = name.trim();
    return kept.length() > MAX_SHOWN_LENGTH ? kept.substring(0, MAX_SHOWN_LENGTH) : kept;
  }
}
