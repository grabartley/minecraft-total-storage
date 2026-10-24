package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.terminal.TerminalMessages;
import java.util.Optional;
import net.minecraft.text.Text;

public enum TerminalOpenState {
  READY,

  MISSING,
  UNPOWERED,
  DENIED,

  REFUSED;

  public boolean allowed() {
    return this == READY;
  }

  public Optional<Text> refusal() {
    return switch (this) {
      case UNPOWERED -> Optional.of(TerminalMessages.unpowered());
      case DENIED -> Optional.of(TerminalMessages.accessDenied());
      case READY, MISSING, REFUSED -> Optional.empty();
    };
  }
}
