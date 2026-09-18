package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.listing.ListingWindow;

public final class TerminalSettingsState {
  private ListingWindow window = ListingWindow.EMPTY;
  private boolean showing;

  public void showing(ListingWindow requested) {
    showing = true;
    window = requested == null ? ListingWindow.EMPTY : requested;
  }

  public ListingWindow window() {
    return window;
  }

  public boolean isOpen() {
    return showing;
  }

  public void closed() {
    showing = false;
    window = ListingWindow.EMPTY;
  }

  @Override
  public String toString() {
    return "TerminalSettingsState[showing=" + showing + ", window=" + window + "]";
  }
}
