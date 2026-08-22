package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Applies a filter to a built index, keeping index order.
 *
 * <p>The mapping from variant to candidate is supplied rather than known, because flattening an
 * item variant needs a registry and this side of the seam deliberately has none. See ADR 0012.
 *
 * @see FilterEvaluator
 */
public final class NetworkIndexFilter {
  private NetworkIndexFilter() {}

  public static <V> List<NetworkIndexEntry<V>> apply(
      NetworkIndex<V> index, Function<V, FilterCandidate> toCandidate, ItemFilter filter) {
    Objects.requireNonNull(index, "index");
    Objects.requireNonNull(toCandidate, "toCandidate");
    Objects.requireNonNull(filter, "filter");
    if (filter.isEmpty()) {
      return index.entries();
    }
    return index.matching(
        entry -> FilterEvaluator.accepts(toCandidate.apply(entry.variant()), filter));
  }
}
