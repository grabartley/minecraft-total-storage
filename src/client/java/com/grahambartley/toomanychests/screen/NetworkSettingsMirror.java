package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkSettingsView;

/**
 * The settings panel this client is drawing, as the server last described it.
 *
 * <p>A display cache and nothing more, on the same terms as the item listing's mirror. Nothing here
 * decides whether an edit is allowed: {@code mayAdminister} greys a control out, and the server
 * decides again when the click arrives, so a modified client that flips it earns buttons that
 * refuse it.
 *
 * <p>Answers are dropped unless they belong to the screen that is open, so a panel arriving after a
 * terminal was closed is not drawn into the next one.
 */
public final class NetworkSettingsMirror {
  public static final int NO_SCREEN = -1;

  private static NetworkSettingsView view = NetworkSettingsView.pending();
  private static int openSyncId = NO_SCREEN;

  private NetworkSettingsMirror() {}

  public static void opened(int syncId) {
    openSyncId = syncId;
    view = NetworkSettingsView.pending();
  }

  /** Returns whether the answer was taken, so a caller drawing it knows there is news. */
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
