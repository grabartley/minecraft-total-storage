package com.grahambartley.totalstorage.listing;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.filter.ItemFilter;
import com.grahambartley.totalstorage.filter.NetworkIndexSearch;
import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
        NetworkIndexSearch.matching(index, toCandidate, filter);
    final List<NetworkIndexEntry<V>> visible = window.sliceOf(matching);
    final List<ListingRow<V>> rows = new ArrayList<>(visible.size());
    for (final NetworkIndexEntry<V> entry : visible) {
      rows.add(new ListingRow<>(entry.variant(), entry.totalCount(), entry.providerCount()));
    }
    final ListingStatus status = ListingStatus.of(index.entries().isEmpty(), !matching.isEmpty());
    return new ListingPage<>(revision, status, window.offset(), matching.size(), rows);
  }
}
