package com.grahambartley.toomanychests.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Puts what a player was most likely looking for at the front of the results.
 *
 * <p>Ordering happens where the results are still whole. A page is a slice of the order, so a
 * client sorting the rows it was sent could only ever rank one screenful against itself, and the
 * best answer would sit on whichever page it happened to land on.
 *
 * <p>Rows of equal strength keep the order they arrived in, which is the index's own. That is what
 * keeps a grid stable while a player types: a character that narrows nothing leaves the rows where
 * they were rather than reshuffling equals.
 *
 * <p>A query with no bare text is returned untouched. Tags and mod identifiers say whether a row
 * belongs rather than how well it fits, so there is nothing to rank by and the index order stands.
 */
public final class SearchOrdering {
  private SearchOrdering() {}

  /**
   * @param matches the rows a filter accepted, in index order
   * @param toCandidate how to read one of them as plain data, per ADR 0012
   * @param query what the player typed
   */
  public static <T> List<T> rank(
      List<T> matches, Function<T, FilterCandidate> toCandidate, SearchQuery query) {
    Objects.requireNonNull(matches, "matches");
    Objects.requireNonNull(toCandidate, "toCandidate");
    Objects.requireNonNull(query, "query");
    if (query.nameTerms().isEmpty() || matches.size() < 2) {
      return matches;
    }
    final List<Ranked<T>> ranked = new ArrayList<>(matches.size());
    for (final T match : matches) {
      ranked.add(new Ranked<>(match, SearchRanking.of(toCandidate.apply(match), query)));
    }
    ranked.sort(Comparator.comparing(Ranked::strength));
    final List<T> ordered = new ArrayList<>(matches.size());
    for (final Ranked<T> entry : ranked) {
      ordered.add(entry.match());
    }
    return List.copyOf(ordered);
  }

  private record Ranked<T>(T match, MatchStrength strength) {}
}
