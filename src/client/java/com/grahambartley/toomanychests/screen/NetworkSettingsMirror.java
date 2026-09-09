package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkSettingsView;

public final class NetworkSettingsMirror {
  public static final int NO_SCREEN = -1;

  private static NetworkSettingsView view = NetworkSettingsView.pending();
  private static int openSyncId = NO_SCREEN;

  private NetworkSettingsMirror() {}

  public static void opened(int syncId) {
    openSyncId = syncId;
    view = NetworkSettingsView.pending();
  }

  public static boolean accept(int syncId, NetworkSettingsView answered) {
    if (answered == null || openSyncId == NO_SCREEN || syncId != openSyncId) {
      return false;
    }
    view = answered;
    return true;
  }

  public static NetworkSettingsView view() {
    return view;
  }

  public static void closed() {
    openSyncId = NO_SCREEN;
    view = NetworkSettingsView.pending();
  }
}
