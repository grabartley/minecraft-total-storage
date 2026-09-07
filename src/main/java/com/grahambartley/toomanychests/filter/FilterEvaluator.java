package com.grahambartley.toomanychests.filter;

import java.util.ArrayList;
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
 * <p>Whether a row belongs is whether its strength is anything other than none. Asking the two
 * questions separately would mean evaluating the same query twice per row and leaving room for the
 * answers to disagree.
 */
public final class FilterEvaluator {
  private FilterEvaluator() {}

  public static boolean accepts(FilterCandidate candidate, ItemFilter filter) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return true;
    }
    return strengthOf(candidate, filter).isMatch();
  }

  /**
   * How well an item answers the whole view, {@link MatchStrength#NONE} when it does not belong in
   * it at all.
   *
   * <p>The tags and the mod are gates rather than grades: an item either carries them or does not,
   * so passing them leaves the typed text deciding how close the row is.
   */
  public static MatchStrength strengthOf(FilterCandidate candidate, ItemFilter filter) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(filter, "filter");
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

  /** The accepted candidates, in the order they were given. */
  public static List<FilterCandidate> filter(List<FilterCandidate> candidates, ItemFilter filter) {
    Objects.requireNonNull(candidates, "candidates");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return List.copyOf(candidates);
    }
    final List<FilterCandidate> accepted = new ArrayList<>(candidates.size());
    for (final FilterCandidate candidate : candidates) {
      if (accepts(candidate, filter)) {
        accepted.add(candidate);
      }
    }
    return List.copyOf(accepted);
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
