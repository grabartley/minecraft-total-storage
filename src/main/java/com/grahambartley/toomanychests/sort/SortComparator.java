package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.SortOrdering;
import java.util.Comparator;
import java.util.Objects;

/**
 * The one ordering rule the mod sorts by, everywhere it sorts. See ADR 0005.
 *
 * <p>The default is language independent on purpose: ordering by display name means storage
 * rearranges itself when a player changes their game language, and on a server it means whoever
 * sorted last decides the order for everybody. Display name ordering is offered as an explicit
 * choice rather than the default.
 *
 * <p>Every strategy ends in the same total order, so equal holdings compare equal and a stable sort
 * leaves an already sorted collection untouched. That property is the point: an unstable comparator
 * costs a round of transactions and resyncs every time anyone presses sort.
 */
public final class SortComparator {
  private SortComparator() {}

  public static Comparator<SortCandidate> forOrdering(SortOrdering ordering) {
    Objects.requireNonNull(ordering, "ordering");
    return switch (ordering) {
      case REGISTRY_ID -> byCategory().thenComparing(byIdentifier()).thenComparing(byQuantity());
      case DISPLAY_NAME ->
          byCategory()
              .thenComparing(Comparator.comparing(SortCandidate::displayName))
              .thenComparing(byIdentifier())
              .thenComparing(byQuantity());
    };
  }

  private static Comparator<SortCandidate> byCategory() {
    return Comparator.comparingInt(candidate -> candidate.primaryCategory().ordinal());
  }

  private static Comparator<SortCandidate> byIdentifier() {
    return Comparator.comparing(SortCandidate::identifier)
        .thenComparing(SortCandidate::componentsKey);
  }

  /** Full stacks first, then the largest holding, so partials collect at the end for topping up. */
  private static Comparator<SortCandidate> byQuantity() {
    return Comparator.comparing(SortCandidate::isFullStack, Comparator.reverseOrder())
        .thenComparing(Comparator.comparingLong(SortCandidate::count).reversed());
  }
}
