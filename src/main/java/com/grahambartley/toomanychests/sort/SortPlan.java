package com.grahambartley.toomanychests.sort;

import java.util.List;
import java.util.Objects;

public record SortPlan<V>(List<SortHolding<V>> stacks, long total) {
  public SortPlan {
    Objects.requireNonNull(stacks, "stacks");
    stacks = List.copyOf(stacks);
    if (total < 0L) {
      throw new IllegalArgumentException("total must not be negative: " + total);
    }
    long counted = 0L;
    for (final SortHolding<V> stack : stacks) {
      counted += stack.count();
    }
    if (counted != total) {
      throw new IllegalArgumentException(
          "the arranged stacks hold " + counted + " but the plan claims " + total);
    }
  }

  public static <V> SortPlan<V> nothing() {
    return new SortPlan<>(List.of(), 0L);
  }

  public boolean isEmpty() {
    return stacks.isEmpty();
  }

  public int size() {
    return stacks.size();
  }
}
