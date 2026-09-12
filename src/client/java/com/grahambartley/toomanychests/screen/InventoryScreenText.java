package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class InventoryScreenText {
  private static final String PREFIX = "screen." + TooManyChests.MOD_ID + ".inventory.";

  private InventoryScreenText() {}

  public static MutableText of(String suffix, Object... arguments) {
    return Text.translatable(PREFIX + suffix, arguments);
  }
}
