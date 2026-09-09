package com.grahambartley.toomanychests.screen;

import org.lwjgl.glfw.GLFW;

public final class AmountEntry {
  private final int ceiling;

  private String typed = "";

  public AmountEntry(int ceiling) {
    this.ceiling = Math.max(0, ceiling);
  }

  public int ceiling() {
    return ceiling;
  }

  public String text() {
    return typed;
  }

  public int value() {
    return typed.isEmpty() ? 0 : Integer.parseInt(typed);
  }

  public boolean isReady() {
    return value() > 0;
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
    if (typedCharacter < '0' || typedCharacter > '9' || ceiling == 0) {
      return false;
    }
    final String next = typed + typedCharacter;
    if (next.length() > String.valueOf(ceiling).length()) {
      return false;
    }
    final int value = Integer.parseInt(next);
    if (value > ceiling) {
      return false;
    }
    final String kept = value == 0 ? "" : next;
    if (kept.equals(typed)) {
      return false;
    }
    typed = kept;
    return true;
  }

  public boolean backspace() {
    if (typed.isEmpty()) {
      return false;
    }
    typed = typed.substring(0, typed.length() - 1);
    return true;
  }
}
