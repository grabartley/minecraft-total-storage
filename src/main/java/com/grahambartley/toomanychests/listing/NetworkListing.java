package com.grahambartley.toomanychests.listing;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.filter.NetworkIndexFilter;
import com.grahambartley.toomanychests.filter.SearchOrdering;
import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Turns an index and what a player has narrowed it to into the one page they are looking at.
 *
 * <p>Searching and filtering happen here, on the whole index, rather than on the client against a
 * page it already holds. A client can only filter what it was sent, so filtering there would mean
 * sending everything, which is the cost this whole mechanism exists to avoid. It also means a page
 * is always drawn from the network as it stands rather than from whatever the client last cached.
 *
 * <p>The results are ranked before they are paged, per ADR 0028. A page is a slice of the order, so
 * ranking after the slice was taken would only ever sort one screenful against itself.
 *
 * <p>Per ADR 0012 the mapping from variant to candidate is supplied, so nothing here needs a
 * registry and the whole computation is testable without a world.
 */
public final class NetworkListing {
  private NetworkListing() {}

  public static <V> ListingPage<V> page(
      NetworkIndex<V> index,
      Function<V, FilterCandidate> toCandidate,
      ItemFilter filter,
      ListingWindow window,
      long revision) {
    Objects.requireNonNull(index, "index");
    Objects.requireNonNull(toCandidate, "toCandidate");
    Objects.requireNonNull(filter, "filter");
    Objects.requireNonNull(window, "window");

    final List<NetworkIndexEntry<V>> matching =
        NetworkIndexFilter.apply(index, toCandidate, filter);
    final List<NetworkIndexEntry<V>> ranked =
        SearchOrdering.rank(matching, entry -> toCandidate.apply(entry.variant()), filter.query());
    final List<NetworkIndexEntry<V>> visible = window.sliceOf(ranked);
    final List<ListingRow<V>> rows = new ArrayList<>(visible.size());
    for (final NetworkIndexEntry<V> entry : visible) {
      rows.add(new ListingRow<>(entry.variant(), entry.totalCount(), entry.providerCount()));
    }
    final ListingStatus status = ListingStatus.of(index.entries().isEmpty(), !matching.isEmpty());
    return new ListingPage<>(revision, status, window.offset(), matching.size(), rows);
  }
}
