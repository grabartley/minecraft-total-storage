package com.grahambartley.totalstorage.index;

import com.grahambartley.totalstorage.storage.ContainerScanResult;
import com.grahambartley.totalstorage.storage.StorageProvider;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public final class NetworkIndexScan {
  private final Comparator<NetworkIndexEntry<ItemVariant>> ordering;

  public NetworkIndexScan() {
    this(new ItemVariantSortKey());
  }

  public NetworkIndexScan(Function<ItemVariant, String> sortKey) {
    this.ordering = NetworkIndexOrdering.byKey(Objects.requireNonNull(sortKey, "sortKey"));
  }

  public NetworkIndex<ItemVariant> index(ContainerScanResult scan) {
    Objects.requireNonNull(scan, "scan");
    return index(scan.providers());
  }

  public NetworkIndex<ItemVariant> index(List<StorageProvider> providers) {
    Objects.requireNonNull(providers, "providers");
    final NetworkIndexBuilder<ItemVariant> builder = new NetworkIndexBuilder<>();
    for (final StorageProvider provider : providers) {
      for (final StorageView<ItemVariant> view : provider.storage().nonEmptyViews()) {
        builder.add(view.getResource(), view.getAmount(), provider.origin());
      }
    }
    return builder.build(ordering);
  }
}
