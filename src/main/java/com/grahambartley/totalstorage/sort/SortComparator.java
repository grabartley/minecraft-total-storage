package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.config.SortOrdering;
import java.util.Comparator;
import java.util.Objects;

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

  private static Comparator<SortCandidate> byQuantity() {
    return Comparator.comparing(SortCandidate::isFullStack, Comparator.reverseOrder())
        .thenComparing(Comparator.comparingLong(SortCandidate::count).reversed());
  }
}
