package com.grahambartley.toomanychests.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One quantity of one variant, on its way through a sort.
 *
 * <p>Generic over the variant per ADR 0012, so the merging and splitting arithmetic is unit
 * testable without a registry. Production uses {@code ItemVariant}.
 *
 * <p>The quantity lives on the candidate rather than beside it, because the comparator orders by
 * how much a holding carries and two copies of that number would eventually disagree.
 *
 * @param variant what the holding is, compared by equality when merging
 * @param candidate the same holding flattened into what the comparator reads
 */
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

  /** Merges another holding of the same variant into this one. */
  public SortHolding<V> plus(SortHolding<V> other) {
    Objects.requireNonNull(other, "other");
    if (!variant.equals(other.variant)) {
      throw new IllegalArgumentException("only holdings of one variant merge: " + other.variant);
    }
    return new SortHolding<>(variant, candidate.withCount(count() + other.count()));
  }

  /**
   * Breaks the holding into the stacks it occupies, full ones first. A merged holding is one number
   * and a container is a row of slots, so this is where the number becomes slots again.
   */
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
