package com.grahambartley.totalstorage.screen;

import org.lwjgl.glfw.GLFW;

public final class PromptModifiers {
  private PromptModifiers() {}

  public static boolean isSelectAllHeld(int modifiers) {
    return (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
  }
}
