package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Answers what a network files an item under, without walking the tag set every time it is asked.
 *
 * <p>The terminal asks this once per item per page render, so the walk is worth avoiding, and the
 * answer only changes when the set does. Editing the set throws the cache away wholesale rather
 * than working out which items an edit touched, because a tag set is small and a wrong entry here
 * would be a filter column that quietly disagrees with the network.
 *
 * <p>Server thread only, like the rest of a terminal's live state.
 */
public final class NetworkTagLookup {
  private final Map<String, Set<String>> cache = new HashMap<>();

  private NetworkTagSet tags;

  public NetworkTagLookup(NetworkTagSet tags) {
    this.tags = Objects.requireNonNull(tags, "tags");
  }

  public NetworkTagSet tags() {
    return tags;
  }

  /** Points the lookup at an edited set, discarding answers computed from the old one. */
  public void update(NetworkTagSet updated) {
    Objects.requireNonNull(updated, "updated");
    if (updated.equals(tags)) {
      return;
    }
    tags = updated;
    cache.clear();
  }

  /** Empty rather than null when the network files the item under nothing. */
  public Set<String> tagIdsFor(String itemId) {
    final String item = FilterText.normalise(itemId);
    return item.isEmpty() ? Set.of() : cache.computeIfAbsent(item, tags::tagIdsFor);
  }
}
