package com.grahambartley.toomanychests.screen;

import org.lwjgl.glfw.GLFW;

public final class AmountEntry {
  public static final int MAX_DIGITS = 9;

  private final int ceiling;

  private final PromptEntryBuffer buffer = new PromptEntryBuffer("");

  public AmountEntry(int ceiling) {
    this.ceiling = Math.max(0, ceiling);
  }

  public int ceiling() {
    return ceiling;
  }

  public String text() {
    return buffer.text();
  }

  public boolean takesEverything() {
    return buffer.isEmpty();
  }

  public int requested() {
    return takesEverything() ? ceiling : Integer.parseInt(buffer.text());
  }

  public int value() {
    return Math.min(requested(), ceiling);
  }

  public boolean asksForMoreThanThereIs() {
    return requested() > ceiling;
  }

  public boolean isReady() {
    return value() > 0;
  }

  public PromptKey keyPressed(int keyCode, boolean controlDown) {
    if (controlDown && keyCode == GLFW.GLFW_KEY_A) {
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
    if (typedCharacter < '0' || typedCharacter > '9' || ceiling == 0) {
      return false;
    }
    final String candidate = buffer.candidate(typedCharacter);
    if (candidate.length() > MAX_DIGITS) {
      return false;
    }
    final String next = withoutLeadingZeros(candidate);
    if (!buffer.isReplacing() && next.equals(buffer.text())) {
      return false;
    }
    buffer.accept(next);
    return true;
  }

  public boolean backspace() {
    return buffer.backspace();
  }

  public boolean selectAll() {
    return buffer.selectAll();
  }

  private static String withoutLeadingZeros(String candidate) {
    int start = 0;
    while (start < candidate.length() && candidate.charAt(start) == '0') {
      start++;
    }
    return candidate.substring(start);
  }
}
