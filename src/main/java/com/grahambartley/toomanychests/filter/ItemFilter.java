package com.grahambartley.toomanychests.filter;

import java.util.Set;

public record ItemFilter(
    Set<String> activeTags, TagCombinationMode combinationMode, SearchQuery query) {
  public static final ItemFilter NONE =
      new ItemFilter(Set.of(), TagCombinationMode.DEFAULT, SearchQuery.EMPTY);

  public ItemFilter {
    activeTags = FilterText.tagIds(activeTags);
    combinationMode = combinationMode == null ? TagCombinationMode.DEFAULT : combinationMode;
    query = query == null ? SearchQuery.EMPTY : query;
  }

  public static ItemFilter ofTags(TagCombinationMode combinationMode, Set<String> activeTags) {
    return new ItemFilter(activeTags, combinationMode, SearchQuery.EMPTY);
  }

  public static ItemFilter ofQuery(SearchQuery query) {
    return new ItemFilter(Set.of(), TagCombinationMode.DEFAULT, query);
  }

  public static ItemFilter ofSearch(String raw) {
    return new ItemFilter(Set.of(), TagCombinationMode.DEFAULT, SearchQueryParser.parse(raw));
  }

  public boolean isEmpty() {
    return activeTags.isEmpty() && query.isEmpty();
  }
}
