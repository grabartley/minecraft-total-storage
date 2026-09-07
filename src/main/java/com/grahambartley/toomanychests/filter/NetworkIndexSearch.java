package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The rows of an index a player's view accepts, closest answer first.
 *
 * <p>Narrowing and ordering are one pass rather than two. Reading an item as plain data means
 * reaching for the registry, so doing it once to decide whether the row belongs and again to decide
 * where it goes would double the cost of every keystroke over the whole network.
 *
 * <p>Rows of equal strength keep index order, because the sort is stable. That is what keeps the
 * grid still while somebody types: a character that narrows nothing leaves the rows where they
 * were, and per ADR 0028 ordering happens here rather than on the page, since a page is a slice of
 * an order that already exists.
 *
 * <p>The mapping from variant to candidate is supplied rather than known, because flattening an
 * item variant needs a registry and this side of the seam deliberately has none. See ADR 0012.
 *
 * @see FilterEvaluator
 */
public final class NetworkIndexSearch {
  private NetworkIndexSearch() {}

  public static <V> List<NetworkIndexEntry<V>> matching(
      NetworkIndex<V> index, Function<V, FilterCandidate> toCandidate, ItemFilter filter) {
    Objects.requireNonNull(index, "index");
    Objects.requireNonNull(toCandidate, "toCandidate");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return index.entries();
    }
    final List<Ranked<V>> ranked = new ArrayList<>();
    for (final NetworkIndexEntry<V> entry : index.entries()) {
      final MatchStrength strength =
          FilterEvaluator.strengthOf(toCandidate.apply(entry.variant()), filter);
      if (strength.isMatch()) {
        ranked.add(new Ranked<>(entry, strength));
      }
    }
    ranked.sort(Comparator.comparing(Ranked::strength));
    final List<NetworkIndexEntry<V>> ordered = new ArrayList<>(ranked.size());
    for (final Ranked<V> match : ranked) {
      ordered.add(match.entry());
    }
    return ordered;
  }

  private record Ranked<V>(NetworkIndexEntry<V> entry, MatchStrength strength) {}
}
