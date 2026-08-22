package com.grahambartley.toomanychests.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Answers whether an item belongs in the player's current view, as a pure decision over plain data.
 *
 * <p>Per ADR 0009 this runs on the server against the live index, because filtering on the client
 * would mean sending every client the whole network, which pagination exists to avoid. ADR 0013
 * covers how the terms of a query combine and why acceptance stops short of ranking.
 */
public final class FilterEvaluator {
  private static final char NAMESPACE_SEPARATOR = ':';

  private FilterEvaluator() {}

  public static boolean accepts(FilterCandidate candidate, ItemFilter filter) {
    Objects.requireNonNull(candidate, "candidate");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return true;
    }
    return filter.combinationMode().matches(candidate.tags(), filter.activeTags())
        && matchesSearch(candidate, filter.query());
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

  private static boolean matchesSearch(FilterCandidate candidate, SearchQuery query) {
    for (final String term : query.nameTerms()) {
      if (!matchesName(candidate, term)) {
        return false;
      }
    }
    for (final String term : query.tagTerms()) {
      if (!matchesAnyTag(candidate.tags(), term)) {
        return false;
      }
    }
    return matchesAnyModId(candidate.modId(), query.modIdTerms());
  }

  /** Namespaced terms match literally, bare terms match fuzzily. See ADR 0013. */
  private static boolean matchesName(FilterCandidate candidate, String term) {
    if (term.indexOf(NAMESPACE_SEPARATOR) >= 0) {
      return candidate.identifier().contains(term);
    }
    return FuzzyNameMatcher.matches(candidate.displayName(), term)
        || FuzzyNameMatcher.matches(candidate.path(), term);
  }

  private static boolean matchesAnyTag(Set<String> tags, String term) {
    for (final String tag : tags) {
      if (FuzzyNameMatcher.matches(tag, term)) {
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
      if (FuzzyNameMatcher.matches(modId, term)) {
        return true;
      }
    }
    return false;
  }
}
