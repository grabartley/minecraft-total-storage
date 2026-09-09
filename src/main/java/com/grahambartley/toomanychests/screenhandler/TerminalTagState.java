package com.grahambartley.toomanychests.screenhandler;

import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalTagState {
  private boolean open;
  private ItemVariant subject;

  public void requested(boolean opened, Optional<ItemVariant> wanted) {
    open = opened;
    subject = opened && wanted != null ? wanted.orElse(null) : null;
  }

  public boolean isOpen() {
    return open;
  }

  public Optional<ItemVariant> subject() {
    return Optional.ofNullable(subject);
  }

  public void closed() {
    open = false;
    subject = null;
  }

  @Override
  public String toString() {
    return "TerminalTagState[open=" + open + ", subject=" + subject + "]";
  }
}
