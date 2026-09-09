package com.grahambartley.toomanychests.filter;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class FilterEvaluator {
  private FilterEvaluator() {}

  public static MatchStrength strengthOf(FilterCandidate candidate, ItemFilter filter) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return MatchStrength.EXACT;
    }
    if (!filter.combinationMode().matches(candidate.tags(), filter.activeTags())) {
      return MatchStrength.NONE;
    }
    final SearchQuery query = filter.query();
    for (final String term : query.tagTerms()) {
      if (!matchesAnyTag(candidate.tags(), term)) {
        return MatchStrength.NONE;
      }
    }
    if (!matchesAnyModId(candidate.modId(), query.modIdTerms())) {
      return MatchStrength.NONE;
    }
    return SearchRanking.of(candidate, query);
  }

  private static boolean matchesAnyTag(Set<String> tags, String term) {
    for (final String tag : tags) {
      if (MatchStrength.of(tag, term).isMatch()) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesAnyModId(String modId, List<String> terms) {
    if (terms.isEmpty()) {
      return true;
    }
    for (final String term : terms) {
      if (MatchStrength.of(modId, term).isMatch()) {
        return true;
      }
    }
    return false;
  }
}
