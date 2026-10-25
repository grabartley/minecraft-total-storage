package com.grahambartley.totalstorage.operation;

public enum TerminalNoticeTone {
  DONE,

  PARTIAL,

  FAILED;

  public boolean isShownInTheTerminal() {
    return this != DONE;
  }
}
