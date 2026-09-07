package com.grahambartley.toomanychests.listing;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

/**
 * The page the open terminal screen is drawing, as this client last heard it.
 *
 * <p>A display cache and nothing more. Per ADR 0009 no click is decided from what is held here: the
 * client sends what the player asked for and the server answers against the live containers, so a
 * page that has gone stale costs a refused request rather than an item that never existed.
 *
 * <p>Pages are dropped unless they belong to the screen that is open and are newer than the one
 * already shown. Both are needed: the first stops a page arriving after a screen closed from being
 * drawn into the next one, and the second stops a slow answer overtaking a fast one while a player
 * scrolls.
 */
public final class TerminalListingMirror {
  /** No terminal screen is open, so no page belongs to anything. */
  public static final int NO_SCREEN = -1;

  private static final ListingCache<ItemVariant> CACHE = new ListingCache<>();

  private static int openSyncId = NO_SCREEN;

  private TerminalListingMirror() {}

  /** A terminal screen has opened, and the pages it asks for are the ones worth keeping. */
  public static void opened(int syncId) {
    openSyncId = syncId;
    CACHE.clear();
  }

  /** Returns whether the page was taken, which is what the screen redraws on. */
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
