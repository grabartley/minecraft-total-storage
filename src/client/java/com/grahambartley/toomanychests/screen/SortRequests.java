package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.network.ModNetworkingClient;

public final class SortRequests {
  private SortRequests() {}

  public static boolean isOffered() {
    return ClientConfigMirror.hasReceived()
        && ClientConfigMirror.current().sortButtonEnabled()
        && ClientPreferencesStore.current().sortButtonVisible();
  }

  public static boolean isAvailable() {
    return isOffered();
  }

  public static boolean send(int syncId) {
    return send(syncId, isOffered());
  }

  public static boolean sendFromKey(int syncId) {
    return send(syncId, ClientConfigMirror.hasReceived());
  }

  private static boolean send(int syncId, boolean drawn) {
    if (!drawn || !isAvailable()) {
      return false;
    }
    ModNetworkingClient.sendSortRequest(syncId);
    return true;
  }
}
