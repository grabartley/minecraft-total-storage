package com.grahambartley.toomanychests.screen;

import org.lwjgl.glfw.GLFW;

/**
 * The number a player is typing into the custom amount prompt, and what each key does to it.
 *
 * <p>A plain buffer rather than a text field, so the prompt survives the screen being laid out
 * again and so what may be typed is a pure function rather than a listener. That also puts the
 * whole of the prompt's behaviour somewhere it can be proven without a running client; the class
 * that draws it holds no rules of its own.
 *
 * <p>Only digits are taken, and a digit that would push the number past what could be withdrawn is
 * refused outright: a field that accepts 9999 and silently hands over 64 has told the player
 * something untrue about what is about to happen.
 */
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

  /** Zero until something has been typed, which is what makes the prompt refuse to confirm. */
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
          isReady() ? PromptKey.CONFIRM : PromptKey.IGNORED;
      case GLFW.GLFW_KEY_BACKSPACE -> backspace() ? PromptKey.EDITED : PromptKey.IGNORED;
      default -> PromptKey.IGNORED;
    };
  }

  public PromptKey charTyped(char typedCharacter) {
    return append(typedCharacter) ? PromptKey.EDITED : PromptKey.IGNORED;
  }

  /** Returns whether the character was taken, so a caller knows the number changed. */
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
    typed = value == 0 ? "" : next;
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
