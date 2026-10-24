package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.filter.FilterText;
import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.Set;

public final class ListingCriteria {
  private ListingWindow window = ListingWindow.EMPTY;
  private String search = "";
  private Set<String> tags = Set.of();
  private TagCombinationMode mode = TagCombinationMode.DEFAULT;

  public ListingWindow window() {
    return window;
  }

  public String search() {
    return search;
  }

  public Set<String> tags() {
    return tags;
  }

  public TagCombinationMode mode() {
    return mode;
  }

  public boolean isNewQuery(String wanted) {
    return !search.equals(normalised(wanted));
  }

  public boolean isNewFilter(Set<String> wantedTags, TagCombinationMode wantedMode) {
    return !tags.equals(FilterText.tagIds(wantedTags)) || mode != orDefault(wantedMode);
  }

  public boolean needsRequest(
      ListingWindow wantedWindow,
      String wantedSearch,
      Set<String> wantedTags,
      TagCombinationMode wantedMode) {
    return isNewQuery(wantedSearch)
        || isNewFilter(wantedTags, wantedMode)
        || !window.equals(orEmpty(wantedWindow));
  }

  public void requested(
      ListingWindow requestedWindow,
      String requestedSearch,
      Set<String> requestedTags,
      TagCombinationMode requestedMode) {
    window = orEmpty(requestedWindow);
    search = normalised(requestedSearch);
    tags = FilterText.tagIds(requestedTags);
    mode = orDefault(requestedMode);
  }

  private static TagCombinationMode orDefault(TagCombinationMode wanted) {
    return wanted == null ? TagCombinationMode.DEFAULT : wanted;
  }

  private static ListingWindow orEmpty(ListingWindow wanted) {
    return wanted == null ? ListingWindow.EMPTY : wanted;
  }

  private static String normalised(String wanted) {
    return wanted == null ? "" : wanted;
  }
}
