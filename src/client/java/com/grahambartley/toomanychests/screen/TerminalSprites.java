package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.util.Identifier;

public final class TerminalSprites {
  private TerminalSprites() {}

  public static Identifier of(String path) {
    return Identifier.of(TooManyChests.MOD_ID, path);
  }
}
