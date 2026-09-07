package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * The translation keys the terminal screen's own words live under.
 *
 * <p>One place assembles the prefix. Every part of the screen needs lines of its own, and a prefix
 * spelled out at each of them is a typo that shows a raw key to a player rather than failing to
 * build.
 */
public final class TerminalScreenText {
  private static final String PREFIX = "screen." + TooManyChests.MOD_ID + ".terminal.";

  private TerminalScreenText() {}

  public static MutableText of(String suffix, Object... arguments) {
    return Text.translatable(PREFIX + suffix, arguments);
  }
}
