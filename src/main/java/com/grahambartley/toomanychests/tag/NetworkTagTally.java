package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterText;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record NetworkTagTally(Map<String, Integer> counts) {
  public static final NetworkTagTally EMPTY = new NetworkTagTally(Map.of());

  public NetworkTagTally {
    counts = counts == null ? Map.of() : Map.copyOf(counts);
  }

  public static NetworkTagTally of(Iterable<FilterCandidate> candidates) {
    Objects.requireNonNull(candidates, "candidates");
    final Map<String, Integer> counted = new HashMap<>();
    for (final FilterCandidate candidate : candidates) {
      for (final String tag : candidate.tags()) {
        counted.merge(tag, 1, Integer::sum);
      }
    }
    return new NetworkTagTally(counted);
  }

  public int countOf(String tagId) {
    return counts.getOrDefault(FilterText.normalise(tagId), 0);
  }
}
