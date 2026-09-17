package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import java.util.Set;

public interface TerminalRequestSender {
  void rows(
      int syncId, ListingWindow window, String search, TagCombinationMode mode, Set<String> tags);

  void settingsShowing(int syncId, int offset, int length);

  void settingsClosed(int syncId);
}
