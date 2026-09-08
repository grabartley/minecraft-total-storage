package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.listing.ListingWindow;

/**
 * What one open settings panel is asking for: the slice of the container list it is showing.
 *
 * <p>Held per screen for the same reason the listing's state is. Two players configuring one
 * terminal scroll its container list independently, and the terminal has no opinion about where
 * either of them is looking.
 *
 * <p>The window is kept rather than re-sent with every refresh, which is what lets an edit by one
 * player push a corrected panel to the other without either of them touching anything.
 */
public final class TerminalSettingsState {
  private ListingWindow window = ListingWindow.EMPTY;

  public void requested(ListingWindow requested) {
    window = requested == null ? ListingWindow.EMPTY : requested;
  }

  public ListingWindow window() {
    return window;
  }

  /**
   * Whether this screen has opened its settings panel at all, since a closed one is not pushed to.
   */
  public boolean isOpen() {
    return !window.isEmpty();
  }

  /** Forgets the panel, so closing it stops the screen being sent settings it is not drawing. */
  public void closed() {
    window = ListingWindow.EMPTY;
  }

  @Override
  public String toString() {
    return "TerminalSettingsState[window=" + window + "]";
  }
}
