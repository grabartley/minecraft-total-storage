package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.util.Identifier;

/**
 * The atlas ids the terminal screen draws from.
 *
 * <p>One place assembles the namespace, for the same reason {@link TerminalScreenText} assembles
 * the translation prefix: a sprite id spelled out at each of the several classes that draw chrome
 * is a typo that shows a missing texture rather than failing to build.
 */
public final class TerminalSprites {
  private TerminalSprites() {}

  public static Identifier of(String path) {
    return Identifier.of(TooManyChests.MOD_ID, path);
  }
}
