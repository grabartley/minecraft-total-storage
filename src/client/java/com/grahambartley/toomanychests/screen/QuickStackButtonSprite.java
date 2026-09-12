package com.grahambartley.toomanychests.screen;

public final class QuickStackButtonSprite {
  private QuickStackButtonSprite() {}

  public static TerminalSprite of(boolean available, boolean hovered, boolean held) {
    if (!available) {
      return TerminalSprite.QUICK_STACK_UNAVAILABLE;
    }
    if (held) {
      return TerminalSprite.QUICK_STACK_PRESSED;
    }
    return hovered ? TerminalSprite.QUICK_STACK_HOVERED : TerminalSprite.QUICK_STACK;
  }
}
