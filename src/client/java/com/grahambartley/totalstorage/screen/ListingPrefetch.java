package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.Objects;

public final class ListingPrefetch {
  private final ListingScroll scroll;
  private final int marginRows;
  private final int guardRows;

  public ListingPrefetch(ListingScroll scroll, int maxLength) {
    this.scroll = Objects.requireNonNull(scroll, "scroll");
    final int fittingRows = Math.min(maxLength, ListingWindow.MAX_LENGTH) / scroll.columns();
    this.marginRows = Math.max(0, (fittingRows - scroll.visibleRows()) / 2);
    this.guardRows = marginRows / 2;
  }

  public ListingWindow window() {
    return around(marginRows);
  }

  public ListingWindow needed() {
    return around(guardRows);
  }

  private ListingWindow around(int paddingRows) {
    final int firstRow = Math.max(0, scroll.topRow() - paddingRows);
    final int endRow = scroll.topRow() + scroll.visibleRows() + paddingRows;
    return ListingWindow.of(firstRow * scroll.columns(), (endRow - firstRow) * scroll.columns());
  }
}
