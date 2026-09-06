package com.grahambartley.toomanychests.terminal;

import net.minecraft.text.Text;

/**
 * Everything a terminal says to the player standing in front of it, in one place.
 *
 * <p>Each line carries its English alongside its translation key, so a player on a server whose
 * resource pack has not caught up is refused in words rather than in a raw key.
 */
public final class TerminalMessages {
  private static final String PREFIX = "block.too-many-chests.terminal.";

  private TerminalMessages() {}

  public static Text unpowered() {
    return line("unpowered", "This terminal needs a redstone signal before it will open");
  }

  public static Text accessDenied() {
    return line("denied", "This terminal belongs to someone else");
  }

  public static Text accessRevoked() {
    return line("revoked", "Your access to that terminal has been taken away");
  }

  private static Text line(String key, String english) {
    return Text.translatableWithFallback(PREFIX + key, english);
  }
}
