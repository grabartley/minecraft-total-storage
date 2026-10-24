package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.filter.FilterText;

public record NetworkTagEntry(
    String id, String displayName, boolean immutable, int assignedItems, int matchingItems) {
  public NetworkTagEntry {
    id = FilterText.tagId(id);
    if (id.isEmpty()) {
      throw new IllegalArgumentException("tag entry id must not be blank");
    }
    displayName = displayName == null || displayName.isBlank() ? id : displayName;
    assignedItems = Math.max(0, assignedItems);
    matchingItems = Math.max(0, matchingItems);
  }

  public boolean isEmptyHere() {
    return matchingItems == 0;
  }
}
