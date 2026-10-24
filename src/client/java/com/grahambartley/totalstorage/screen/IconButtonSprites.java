package com.grahambartley.totalstorage.screen;

import java.util.Objects;

public record IconButtonSprites(
    TerminalSprite normal,
    TerminalSprite hovered,
    TerminalSprite pressed,
    TerminalSprite unavailable) {
  public static final IconButtonSprites QUICK_STACK =
      new IconButtonSprites(
          TerminalSprite.QUICK_STACK,
          TerminalSprite.QUICK_STACK_HOVERED,
          TerminalSprite.QUICK_STACK_PRESSED,
          TerminalSprite.QUICK_STACK_UNAVAILABLE);

  public static final IconButtonSprites SORT =
      new IconButtonSprites(
          TerminalSprite.SORT,
          TerminalSprite.SORT_HOVERED,
          TerminalSprite.SORT_PRESSED,
          TerminalSprite.SORT_UNAVAILABLE);

  public IconButtonSprites {
    Objects.requireNonNull(normal, "normal");
    Objects.requireNonNull(hovered, "hovered");
    Objects.requireNonNull(pressed, "pressed");
    Objects.requireNonNull(unavailable, "unavailable");
  }

  public TerminalSprite select(boolean available, boolean hovered, boolean held) {
    if (!available) {
      return unavailable;
    }
    if (held) {
      return pressed;
    }
    return hovered ? this.hovered : normal;
  }
}
