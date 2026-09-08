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

  public static Text notYours() {
    return line("not-yours", "That terminal is not yours to configure");
  }

  public static Text containerNotInNetwork() {
    return line("container-out-of-range", "This terminal does not reach that container");
  }

  /** Said when a terminal is running at less than it was set to, which is not obvious otherwise. */
  public static Text radiusClamped(int blocks) {
    return Text.translatableWithFallback(
        PREFIX + "radius-clamped", "The server caps this terminal at %s blocks", blocks);
  }

  private static Text line(String key, String english) {
    return Text.translatableWithFallback(PREFIX + key, english);
  }
}
