package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientConfigMirror;
import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import com.grahambartley.totalstorage.network.ModNetworkingClient;

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
