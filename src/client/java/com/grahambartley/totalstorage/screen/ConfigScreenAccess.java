package com.grahambartley.totalstorage.screen;

public record ConfigScreenAccess(boolean serverReceived, boolean serverEditable) {
  public enum Notice {
    NONE,
    NOT_CONNECTED,
    READ_ONLY,
    SAVE_FAILED
  }

  public boolean showsServerSettings() {
    return serverReceived;
  }

  public boolean serverControlsActive() {
    return serverReceived && serverEditable;
  }

  public boolean sendsServerEdits(boolean serverDirty) {
    return serverControlsActive() && serverDirty;
  }

  public Notice notice(boolean preferencesSaveFailed) {
    if (preferencesSaveFailed) {
      return Notice.SAVE_FAILED;
    }
    if (!serverReceived) {
      return Notice.NOT_CONNECTED;
    }
    return serverEditable ? Notice.NONE : Notice.READ_ONLY;
  }
}
