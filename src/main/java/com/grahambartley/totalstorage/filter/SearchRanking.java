package com.grahambartley.totalstorage.filter;

import java.util.Objects;

public final class SearchRanking {
  private SearchRanking() {}

  public static MatchStrength of(FilterCandidate candidate, SearchQuery query, boolean fuzzy) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(query, "query");
    MatchStrength weakest = MatchStrength.EXACT;
    for (final String term : query.nameTerms()) {
      final MatchStrength strength = ofTerm(candidate, term, fuzzy);
      if (strength.compareTo(weakest) > 0) {
        weakest = strength;
      }
    }
    return weakest;
  }

  static MatchStrength ofTerm(FilterCandidate candidate, String term, boolean fuzzy) {
    Objects.requireNonNull(candidate, "candidate");
    if (FilterText.hasNamespace(term)) {
      return MatchStrength.literal(candidate.identifier(), term);
    }
    return MatchStrength.best(
        nameStrength(candidate.displayName(), term, fuzzy),
        nameStrength(candidate.path(), term, fuzzy));
  }

  private static MatchStrength nameStrength(String name, String term, boolean fuzzy) {
    return fuzzy ? MatchStrength.of(name, term) : MatchStrength.literal(name, term);
  }
}
