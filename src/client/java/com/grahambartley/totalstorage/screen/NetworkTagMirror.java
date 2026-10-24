package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.terminal.NetworkTagView;

public final class NetworkTagMirror {
  public static final int NO_SCREEN = -1;

  private static NetworkTagView view = NetworkTagView.pending();
  private static int openSyncId = NO_SCREEN;

  private NetworkTagMirror() {}

  public static void opened(int syncId) {
    openSyncId = syncId;
    view = NetworkTagView.pending();
  }

  public static boolean accept(int syncId, NetworkTagView answered) {
    if (answered == null || openSyncId == NO_SCREEN || syncId != openSyncId) {
      return false;
    }
    view = answered;
    return true;
  }

  public static NetworkTagView view() {
    return view;
  }

  public static void closed() {
    openSyncId = NO_SCREEN;
    view = NetworkTagView.pending();
  }
}
