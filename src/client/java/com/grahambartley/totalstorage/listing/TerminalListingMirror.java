package com.grahambartley.totalstorage.listing;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalListingMirror {
  public static final int NO_SCREEN = -1;

  private static final ListingCache<ItemVariant> CACHE = new ListingCache<>();

  private static int openSyncId = NO_SCREEN;

  private TerminalListingMirror() {}

  public static void opened(int syncId) {
    openSyncId = syncId;
    CACHE.clear();
  }

  public static boolean accept(int syncId, ListingPage<ItemVariant> page) {
    if (page == null || openSyncId == NO_SCREEN || syncId != openSyncId) {
      return false;
    }
    return CACHE.accept(page);
  }

  public static ListingPage<ItemVariant> page() {
    return CACHE.page();
  }

  public static boolean isShowing(int syncId) {
    return openSyncId != NO_SCREEN && openSyncId == syncId;
  }

  public static void closed() {
    openSyncId = NO_SCREEN;
    CACHE.clear();
  }
}
