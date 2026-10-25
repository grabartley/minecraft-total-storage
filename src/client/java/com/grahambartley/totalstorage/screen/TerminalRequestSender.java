package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.Set;

public interface TerminalRequestSender {
  void rows(
      int syncId, ListingWindow window, String search, TagCombinationMode mode, Set<String> tags);

  void settingsShowing(int syncId, int offset, int length);
}
