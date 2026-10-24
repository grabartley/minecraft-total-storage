package com.grahambartley.totalstorage.access;

public final class ServerSettingsAccessRule {
  private ServerSettingsAccessRule() {}

  public static boolean mayEdit(boolean operator, boolean dedicated, boolean host) {
    return operator || (!dedicated && host);
  }
}
