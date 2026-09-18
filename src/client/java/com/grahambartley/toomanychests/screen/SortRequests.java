package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.network.ModNetworkingClient;

public final class SortRequests {
  private SortRequests() {}

  public static boolean isAvailable() {
    return ClientConfigMirror.hasReceived() && ClientConfigMirror.current().sortButtonEnabled();
  }

  public static boolean isOffered() {
    return isAvailable() && ClientPreferencesStore.current().sortButtonVisible();
  }

  public static boolean mayBeOffered() {
    return !ClientConfigMirror.hasReceived() || isOffered();
  }

  public static boolean send(int syncId) {
    if (!isAvailable()) {
      return false;
    }
    ModNetworkingClient.sendSortRequest(syncId);
    return true;
  }
}
