package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalTagState {
  private boolean open;
  private ItemVariant subject;
  private NetworkTagView lastSent;

  public void requested(boolean opened, Optional<ItemVariant> wanted) {
    open = opened;
    subject = opened && wanted != null ? wanted.orElse(null) : null;
    lastSent = null;
  }

  public void sent(NetworkTagView view) {
    lastSent = Objects.requireNonNull(view, "view");
  }

  public boolean differsFromLastSent(NetworkTagView view) {
    return !Objects.equals(lastSent, view);
  }

  public Optional<NetworkTagView> lastSent() {
    return Optional.ofNullable(lastSent);
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
    lastSent = null;
  }

  @Override
  public String toString() {
    return "TerminalTagState[open=" + open + ", subject=" + subject + "]";
  }
}
