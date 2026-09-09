package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
    return List.copyOf(ordered);
  }

  private record Ranked<V>(NetworkIndexEntry<V> entry, MatchStrength strength) {}
}
