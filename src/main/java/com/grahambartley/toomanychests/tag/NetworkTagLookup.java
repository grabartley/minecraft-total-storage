package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NetworkTagLookup {
  private final Map<String, Set<String>> cache = new HashMap<>();

  private NetworkTagSet tags;

  public NetworkTagLookup(NetworkTagSet tags) {
    this.tags = Objects.requireNonNull(tags, "tags");
  }

  public NetworkTagSet tags() {
    return tags;
  }

  public void update(NetworkTagSet updated) {
    Objects.requireNonNull(updated, "updated");
    if (updated.equals(tags)) {
      return;
    }
    tags = updated;
    cache.clear();
  }

  public Set<String> tagIdsFor(String itemId) {
    final String item = FilterText.normalise(itemId);
    return item.isEmpty() ? Set.of() : cache.computeIfAbsent(item, tags::tagIdsFor);
  }
}
