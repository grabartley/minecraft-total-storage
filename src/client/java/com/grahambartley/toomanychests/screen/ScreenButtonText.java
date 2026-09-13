package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class ScreenButtonText {
  private static final String PREFIX = "screen." + TooManyChests.MOD_ID + ".button.";

  private ScreenButtonText() {}

  public static MutableText of(String suffix, Object... arguments) {
    return Text.translatable(PREFIX + suffix, arguments);
  }
}
