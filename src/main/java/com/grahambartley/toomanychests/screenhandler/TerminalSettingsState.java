package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.listing.ListingWindow;

public final class TerminalSettingsState {
  private ListingWindow window = ListingWindow.EMPTY;

  public void requested(ListingWindow requested) {
    window = requested == null ? ListingWindow.EMPTY : requested;
  }

  public ListingWindow window() {
    return window;
  }

  public boolean isOpen() {
    return !window.isEmpty();
  }

  public void closed() {
    window = ListingWindow.EMPTY;
  }

  @Override
  public String toString() {
    return "TerminalSettingsState[window=" + window + "]";
  }
}
