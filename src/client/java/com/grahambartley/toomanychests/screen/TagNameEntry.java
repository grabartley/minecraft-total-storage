package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.tag.TagNameRule;
import org.lwjgl.glfw.GLFW;

public final class TagNameEntry {
  private String typed;

  public TagNameEntry(String initial) {
    typed = trimmed(initial);
  }

  public String text() {
    return typed;
  }

  public boolean isReady() {
    return !typed.isBlank();
  }

  public PromptKey keyPressed(int keyCode) {
    return switch (keyCode) {
      case GLFW.GLFW_KEY_ESCAPE -> PromptKey.CANCEL;
      case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
          isReady() ? PromptKey.CONFIRM : PromptKey.HANDLED;
      case GLFW.GLFW_KEY_BACKSPACE -> {
        backspace();
        yield PromptKey.HANDLED;
      }
      default -> PromptKey.HANDLED;
    };
  }

  public PromptKey charTyped(char typedCharacter) {
    append(typedCharacter);
    return PromptKey.HANDLED;
  }

  public boolean append(char typedCharacter) {
    if (!isWritable(typedCharacter) || typed.length() >= TagNameRule.MAX_LENGTH) {
      return false;
    }
    if (typedCharacter == ' ' && (typed.isEmpty() || typed.endsWith(" "))) {
      return false;
    }
    typed = typed + typedCharacter;
    return true;
  }

  public boolean backspace() {
    if (typed.isEmpty()) {
      return false;
    }
    typed = typed.substring(0, typed.length() - 1);
    return true;
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
