package com.grahambartley.toomanychests.filter;

import java.util.Objects;

public final class SearchRanking {
  private SearchRanking() {}

  public static MatchStrength of(FilterCandidate candidate, SearchQuery query) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(query, "query");
    MatchStrength weakest = MatchStrength.EXACT;
    for (final String term : query.nameTerms()) {
      final MatchStrength strength = ofTerm(candidate, term);
      if (strength.compareTo(weakest) > 0) {
        weakest = strength;
      }
    }
    return weakest;
  }

  static MatchStrength ofTerm(FilterCandidate candidate, String term) {
    Objects.requireNonNull(candidate, "candidate");
    if (FilterText.hasNamespace(term)) {
      return MatchStrength.literal(candidate.identifier(), term);
    }
    return MatchStrength.best(
        MatchStrength.of(candidate.displayName(), term), MatchStrength.of(candidate.path(), term));
  }
}
