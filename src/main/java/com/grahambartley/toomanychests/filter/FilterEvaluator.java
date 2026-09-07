package com.grahambartley.toomanychests.filter;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Answers how well an item belongs in the player's current view, as a pure decision over plain
 * data.
 *
 * <p>Per ADR 0009 this runs on the server against the live index, because filtering on the client
 * would mean sending every client the whole network, which pagination exists to avoid. ADR 0013
 * covers how the terms of a query combine, and ADR 0028 why one answer serves both whether a row
 * belongs and where it is shown.
 *
 * <p>There is one question rather than two, because whether a row belongs is whether its strength
 * is anything other than none. Asking separately would evaluate the same query twice per row and
 * leave room for the answers to disagree.
 */
public final class FilterEvaluator {
  private FilterEvaluator() {}

  /**
   * How well an item answers the whole view, {@link MatchStrength#NONE} when it does not belong in
   * it at all.
   *
   * <p>The tags and the mod are gates rather than grades: an item either carries them or does not,
   * so passing them leaves the typed text deciding how close the row is. A view narrowed to nothing
   * grades every row alike, since there is no question for one to answer better than another.
   */
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
