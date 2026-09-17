package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import java.util.Set;

public final class TerminalRequests {
  private final int syncId;
  private final ListingCriteria listing = new ListingCriteria();

  private ListingWindow containers = ListingWindow.EMPTY;
  private boolean settingsShowing;

  public TerminalRequests(int syncId) {
    this.syncId = syncId;
  }

  public boolean isNewListing(String search, Set<String> tags, TagCombinationMode mode) {
    return listing.isNewQuery(search) || listing.isNewFilter(tags, mode);
  }

  public boolean needsRows(
      ListingWindow window, String search, Set<String> tags, TagCombinationMode mode) {
    return listing.needsRequest(window, search, tags, mode);
  }

  public void rows(ListingWindow window, String search, Set<String> tags, TagCombinationMode mode) {
    listing.requested(window, search, tags, mode);
    ModNetworkingClient.sendListingRequest(
        syncId, listing.window(), listing.search(), listing.mode(), listing.tags());
  }

  public boolean isSettingsShowing() {
    return settingsShowing;
  }

  public boolean needsContainers(ListingWindow window) {
    return !containers.equals(window);
  }

  public void showSettings(ListingWindow window) {
    settingsShowing = true;
    containers = window == null ? ListingWindow.EMPTY : window;
    ModNetworkingClient.sendSettingsRequest(syncId, containers.offset(), containers.length());
  }

  public void hideSettings() {
    if (!settingsShowing) {
      return;
    }
    settingsShowing = false;
    containers = ListingWindow.EMPTY;
    ModNetworkingClient.sendSettingsClosed(syncId);
  }
}
