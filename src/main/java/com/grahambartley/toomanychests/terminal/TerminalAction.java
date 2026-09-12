package com.grahambartley.toomanychests.terminal;

public enum TerminalAction {
  RESCAN,

  HIGHLIGHT,

  SORT_NETWORK,

  CONSOLIDATE;

  public boolean isNetworkOperation() {
    return this == SORT_NETWORK || this == CONSOLIDATE;
  }
}
