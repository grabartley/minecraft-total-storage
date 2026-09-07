package com.grahambartley.toomanychests.filter;

import java.util.Objects;

/**
 * How well an item answers what a player typed, which is what decides the order they see.
 *
 * <p>Only the bare text is ranked. A tag or a mod identifier term either holds of an item or does
 * not, so scoring one would sort rows by something the player did not ask a question about.
 *
 * <p>A query of several terms is only as good as its weakest one. Every name term has to match for
 * a row to be shown at all, so the term that barely matched is the one describing how close the row
 * really is, and averaging it away would rank a row above one that answered both terms outright.
 */
public final class SearchRanking {
  private SearchRanking() {}

  /**
   * How well a candidate answers a whole query, {@link MatchStrength#EXACT} when it asked for no
   * text at all.
   */
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

  /**
   * How well a candidate answers one bare term, taking the better of its display name and the path
   * of its identifier. Per ADR 0013 the namespace is not searched, so a term carrying one is read
   * as naming an item outright and matched literally against the whole identifier.
   */
  static MatchStrength ofTerm(FilterCandidate candidate, String term) {
    Objects.requireNonNull(candidate, "candidate");
    if (FilterText.hasNamespace(term)) {
      return MatchStrength.literal(candidate.identifier(), term);
    }
    return MatchStrength.best(
        MatchStrength.of(candidate.displayName(), term), MatchStrength.of(candidate.path(), term));
  }
}
