package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.access.PlayerNameRule;
import org.lwjgl.glfw.GLFW;

public final class PlayerNameEntry {
  private final PromptEntryBuffer buffer = new PromptEntryBuffer("");

  public String text() {
    return buffer.text();
  }

  public boolean isReady() {
    return !buffer.text().isEmpty();
  }

  public void clear() {
    buffer.accept("");
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
    if (!PlayerNameRule.isNameCharacter(typedCharacter)) {
      return PromptKey.REFUSED;
    }
    final String candidate = buffer.candidate(typedCharacter);
    if (candidate.length() > PlayerNameRule.MAX_TYPED_LENGTH) {
      return PromptKey.REFUSED;
    }
    buffer.accept(candidate);
    return PromptKey.HANDLED;
  }
}
