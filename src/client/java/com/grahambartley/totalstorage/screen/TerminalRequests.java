package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.network.ModNetworkingClient;
import java.util.Objects;
import java.util.Set;

public final class TerminalRequests {
  private static final TerminalRequestSender NETWORKING =
      new TerminalRequestSender() {
        @Override
        public void rows(
            int syncId,
            ListingWindow window,
            String search,
            TagCombinationMode mode,
            Set<String> tags) {
          ModNetworkingClient.sendListingRequest(syncId, window, search, mode, tags);
        }

        @Override
        public void settingsShowing(int syncId, int offset, int length) {
          ModNetworkingClient.sendSettingsRequest(syncId, offset, length);
        }
      };

  private final int syncId;
  private final TerminalRequestSender sender;
  private final ListingCriteria listing = new ListingCriteria();

  private ListingWindow containers = ListingWindow.EMPTY;

  public TerminalRequests(int syncId) {
    this(syncId, NETWORKING);
  }

  TerminalRequests(int syncId, TerminalRequestSender sender) {
    this.syncId = syncId;
    this.sender = Objects.requireNonNull(sender, "sender");
  }

  public boolean isNewListing(String search, Set<String> tags, TagCombinationMode mode) {
    return listing.isNewQuery(search) || listing.isNewFilter(tags, mode);
  }

  public boolean needsRows(
      ListingWindow needed, String search, Set<String> tags, TagCombinationMode mode) {
    return listing.needsRequest(needed, search, tags, mode);
  }

  public void rows(ListingWindow window, String search, Set<String> tags, TagCombinationMode mode) {
    listing.requested(window, search, tags, mode);
    sender.rows(syncId, listing.window(), listing.search(), listing.mode(), listing.tags());
  }

  public boolean needsContainers(ListingWindow needed) {
    return needed.isEmpty() ? !containers.isEmpty() : !containers.covers(needed);
  }

  public void showSettings(ListingWindow window) {
    containers = window == null ? ListingWindow.EMPTY : window;
    sender.settingsShowing(syncId, containers.offset(), containers.length());
  }
}
