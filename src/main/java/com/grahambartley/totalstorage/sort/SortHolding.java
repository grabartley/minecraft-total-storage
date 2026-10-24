package com.grahambartley.totalstorage.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SortHolding<V>(V variant, SortCandidate candidate) {
  public SortHolding {
    Objects.requireNonNull(variant, "variant");
    Objects.requireNonNull(candidate, "candidate");
    if (candidate.count() <= 0L) {
      throw new IllegalArgumentException("a holding must carry something: " + candidate.count());
    }
  }

  public long count() {
    return candidate.count();
  }

  public SortHolding<V> plus(SortHolding<V> other) {
    Objects.requireNonNull(other, "other");
    if (!variant.equals(other.variant)) {
      throw new IllegalArgumentException("only holdings of one variant merge: " + other.variant);
    }
    return new SortHolding<>(variant, candidate.withCount(count() + other.count()));
  }

  public List<SortHolding<V>> split() {
    final int perStack = candidate.maxCount();
    final List<SortHolding<V>> stacks = new ArrayList<>();
    long remaining = count();
    while (remaining > 0L) {
      final long taken = Math.min(remaining, perStack);
      stacks.add(new SortHolding<>(variant, candidate.withCount(taken)));
      remaining -= taken;
    }
    return List.copyOf(stacks);
  }
}
