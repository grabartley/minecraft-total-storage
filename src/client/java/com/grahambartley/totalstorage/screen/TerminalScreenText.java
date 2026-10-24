package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class TerminalScreenText {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".terminal.";

  private TerminalScreenText() {}

  public static MutableText of(String suffix, Object... arguments) {
    return Text.translatable(PREFIX + suffix, arguments);
  }
}
