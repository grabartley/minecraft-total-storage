package com.grahambartley.totalstorage.screen;

public final class DepositAllKey {
  private DepositAllKey() {}

  public static boolean deposits(
      boolean bound, boolean settingsTab, boolean tagging, boolean searchHasTheKeyboard) {
    return bound && !settingsTab && !tagging && !searchHasTheKeyboard;
  }
}
