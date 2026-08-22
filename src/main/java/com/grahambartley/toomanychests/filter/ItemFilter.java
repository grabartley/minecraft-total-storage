package com.grahambartley.toomanychests.filter;

import java.util.Set;

/**
 * The view a player has narrowed the terminal to: the tags they clicked, how those combine, and
 * what they typed.
 *
 * <p>Search and tags always combine with AND, so typing narrows within the selected tags rather
 * than searching past them. Only the tags themselves honour {@link TagCombinationMode}.
 *
 * @param activeTags the tags selected in the filter column, empty when none are
 * @param combinationMode how the active tags combine with each other
 * @param query the parsed contents of the search box
 */
public record ItemFilter(
    Set<String> activeTags, TagCombinationMode combinationMode, SearchQuery query) {
  /** No tags selected and nothing typed, which matches the whole network. */
  public static final ItemFilter NONE =
      new ItemFilter(Set.of(), TagCombinationMode.DEFAULT, SearchQuery.EMPTY);

  public ItemFilter {
    activeTags = FilterText.normaliseAll(activeTags);
    combinationMode = combinationMode == null ? TagCombinationMode.DEFAULT : combinationMode;
    query = query == null ? SearchQuery.EMPTY : query;
  }

  public static ItemFilter ofTags(TagCombinationMode combinationMode, Set<String> activeTags) {
    return new ItemFilter(activeTags, combinationMode, SearchQuery.EMPTY);
  }

  public static ItemFilter ofSearch(String raw) {
    return new ItemFilter(Set.of(), TagCombinationMode.DEFAULT, SearchQueryParser.parse(raw));
  }

  /** True when nothing has been selected or typed, which lets callers skip evaluation entirely. */
  public boolean isEmpty() {
    return activeTags.isEmpty() && query.isEmpty();
  }
}
