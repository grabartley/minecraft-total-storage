package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerNameRule;
import org.lwjgl.glfw.GLFW;

public final class ContainerNameEntry {
  private final PromptEntryBuffer buffer;

  public ContainerNameEntry(String initial) {
    buffer = new PromptEntryBuffer(ContainerNameRule.sanitise(initial));
  }

  public String text() {
    return buffer.text();
  }

  public String name() {
    return ContainerNameRule.sanitise(buffer.text());
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    if (selectAllHeld && keyCode == GLFW.GLFW_KEY_A) {
      return buffer.selectAll() ? PromptKey.HANDLED : PromptKey.REFUSED;
    }
    return switch (keyCode) {
      case GLFW.GLFW_KEY_ESCAPE -> PromptKey.CANCEL;
      case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> PromptKey.CONFIRM;
      case GLFW.GLFW_KEY_BACKSPACE -> buffer.backspace() ? PromptKey.HANDLED : PromptKey.REFUSED;
      default -> PromptKey.HANDLED;
    };
  }

  public PromptKey charTyped(char typedCharacter) {
    return append(typedCharacter) ? PromptKey.HANDLED : PromptKey.REFUSED;
  }

  public boolean append(char typedCharacter) {
    if (Character.isISOControl(typedCharacter)) {
      return false;
    }
    final boolean space = Character.isWhitespace(typedCharacter);
    final String candidate = buffer.candidate(space ? ' ' : typedCharacter);
    if (candidate.length() > ContainerNameRule.MAX_LENGTH) {
      return false;
    }
    if (space && (candidate.length() == 1 || candidate.endsWith("  "))) {
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
}
