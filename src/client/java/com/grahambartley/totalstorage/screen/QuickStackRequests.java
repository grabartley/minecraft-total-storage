package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientConfigMirror;
import com.grahambartley.totalstorage.network.ModNetworkingClient;

public final class QuickStackRequests {
  private QuickStackRequests() {}

  public static boolean isOffered() {
    return ClientConfigMirror.hasReceived();
  }

  public static boolean isAvailable() {
    return ClientConfigMirror.current().quickStackEnabled();
  }

  public static void send() {
    if (isOffered() && isAvailable()) {
      ModNetworkingClient.sendQuickStackRequest();
    }
  }
}
