package com.grahambartley.totalstorage.operation;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public final class HeldResources {
  private HeldResources() {}

  public static <V> Set<V> of(Storage<V> storage) {
    if (storage == null) {
      return Set.of();
    }
    final Set<V> held = new HashSet<>();
    for (final StorageView<V> view : storage.nonEmptyViews()) {
      if (!view.isResourceBlank() && view.getAmount() > 0L) {
        held.add(view.getResource());
      }
    }
    return Set.copyOf(held);
  }
}
