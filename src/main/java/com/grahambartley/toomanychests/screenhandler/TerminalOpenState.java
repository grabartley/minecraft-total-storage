package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.terminal.TerminalMessages;
import java.util.Optional;
import net.minecraft.text.Text;

/** Whether a terminal will open for a player right now, and what to tell them when it will not. */
public enum TerminalOpenState {
  READY,
  /** There is no terminal at that position any more. */
  MISSING,
  UNPOWERED,
  DENIED,
  /**
   * The terminal would have opened, but vanilla declined to give this player a screen at all,
   * because they are spectating or already have one open. Vanilla owns that refusal, so nothing is
   * said here that would contradict it.
   */
  REFUSED;

  public boolean allowed() {
    return this == READY;
  }

  /**
   * Empty for a terminal that opens, and for one that is not there: a player right-clicking a
   * position that holds no terminal has already been told by the block that vanished.
   */
  public Optional<Text> refusal() {
    return switch (this) {
      case UNPOWERED -> Optional.of(TerminalMessages.unpowered());
      case DENIED -> Optional.of(TerminalMessages.accessDenied());
      case READY, MISSING, REFUSED -> Optional.empty();
    };
  }
}
