package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.network.ModNetworkingClient;

public final class SortRequests {
  private SortRequests() {}

  public static boolean isOffered() {
    return ClientConfigMirror.hasReceived() && ClientPreferencesStore.current().sortButtonVisible();
  }

  public static boolean isAvailable() {
    return ClientConfigMirror.current().sortButtonEnabled();
  }

  public static void send(int syncId) {
    if (isOffered() && isAvailable()) {
      ModNetworkingClient.sendSortRequest(syncId);
    }
  }

  public static void sendFromKey(int syncId) {
    if (ClientConfigMirror.hasReceived() && isAvailable()) {
      ModNetworkingClient.sendSortRequest(syncId);
    }
  }
}
