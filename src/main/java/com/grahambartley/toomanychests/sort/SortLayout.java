package com.grahambartley.toomanychests.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SortLayout {
  private SortLayout() {}

  public static <V> List<SortHolding<V>> arrange(
      List<SortHolding<V>> holdings, Comparator<SortCandidate> comparator) {
    Objects.requireNonNull(holdings, "holdings");
    Objects.requireNonNull(comparator, "comparator");
    if (holdings.isEmpty()) {
      return List.of();
    }
    final List<SortHolding<V>> merged = mergeByVariant(holdings);
    merged.sort(Comparator.comparing(SortHolding::candidate, comparator));
    final List<SortHolding<V>> stacks = new ArrayList<>(merged.size());
    for (final SortHolding<V> holding : merged) {
      stacks.addAll(holding.split());
    }
    return List.copyOf(stacks);
  }

  public static <V> long totalOf(List<SortHolding<V>> holdings) {
    long total = 0L;
    for (final SortHolding<V> holding : holdings) {
      total += holding.count();
    }
    return total;
  }

  private static <V> List<SortHolding<V>> mergeByVariant(List<SortHolding<V>> holdings) {
    final Map<V, SortHolding<V>> merged = new LinkedHashMap<>();
    for (final SortHolding<V> holding : holdings) {
      Objects.requireNonNull(holding, "holding");
      merged.merge(holding.variant(), holding, SortHolding::plus);
    }
    return new ArrayList<>(merged.values());
  }
}
