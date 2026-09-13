package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.network.ModNetworkingClient;

public final class QuickStackRequests {
  private QuickStackRequests() {}

  public static boolean isOffered() {
    return ClientConfigMirror.current().quickStackEnabled();
  }

  public static void send() {
    if (isOffered()) {
      ModNetworkingClient.sendQuickStackRequest();
    }
  }
}
