package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.tag.TagNameRule;
import org.lwjgl.glfw.GLFW;

public final class TagNameEntry {
  private final PromptEntryBuffer buffer;

  public TagNameEntry(String initial) {
    buffer = new PromptEntryBuffer(trimmed(initial));
  }

  public String text() {
    return buffer.text();
  }

  public boolean isReady() {
    return !buffer.text().isBlank();
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    if (selectAllHeld && keyCode == GLFW.GLFW_KEY_A) {
      return buffer.selectAll() ? PromptKey.HANDLED : PromptKey.REFUSED;
    }
    return switch (keyCode) {
      case GLFW.GLFW_KEY_ESCAPE -> PromptKey.CANCEL;
      case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
          isReady() ? PromptKey.CONFIRM : PromptKey.REFUSED;
      case GLFW.GLFW_KEY_BACKSPACE -> buffer.backspace() ? PromptKey.HANDLED : PromptKey.REFUSED;
      default -> PromptKey.HANDLED;
    };
  }

  public PromptKey charTyped(char typedCharacter) {
    return append(typedCharacter) ? PromptKey.HANDLED : PromptKey.REFUSED;
  }

  public boolean append(char typedCharacter) {
    if (!isWritable(typedCharacter)) {
      return false;
    }
    final String candidate = buffer.candidate(typedCharacter);
    if (candidate.length() > TagNameRule.MAX_LENGTH) {
      return false;
    }
    if (typedCharacter == ' ' && (candidate.length() == 1 || candidate.endsWith("  "))) {
      return false;
    }
    buffer.accept(candidate);
    return true;
  }

  public boolean backspace() {
    return buffer.backspace();
  }

  public boolean selectAll() {
    return buffer.selectAll();
  }

  private static boolean isWritable(char character) {
    return character == ' '
        || character == '_'
        || character == '-'
        || (character >= '0' && character <= '9')
        || (character >= 'a' && character <= 'z')
        || (character >= 'A' && character <= 'Z');
  }

  private static String trimmed(String initial) {
    if (initial == null) {
      return "";
    }
    final String kept = initial.trim();
    return kept.length() > TagNameRule.MAX_LENGTH
        ? kept.substring(0, TagNameRule.MAX_LENGTH)
        : kept;
  }
}
