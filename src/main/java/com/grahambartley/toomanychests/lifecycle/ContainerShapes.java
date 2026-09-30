package com.grahambartley.toomanychests.lifecycle;

import com.grahambartley.toomanychests.storage.ContainerScanResult;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public final class ContainerShapes {
  private final Map<StorageOrigin, OptionalInt> slotCounts = new HashMap<>();

  public void remember(ContainerScanResult scan) {
    Objects.requireNonNull(scan, "scan");
    reshaped(slotCountsOf(scan));
  }

  public List<StorageOrigin> reshaped(ContainerScanResult scan) {
    Objects.requireNonNull(scan, "scan");
    return reshaped(slotCountsOf(scan));
  }

  public List<StorageOrigin> reshaped(Map<StorageOrigin, OptionalInt> current) {
    Objects.requireNonNull(current, "current");
    final List<StorageOrigin> changed = new ArrayList<>();
    current.forEach(
        (origin, slots) -> {
          final OptionalInt before = slotCounts.get(origin);
          if (before != null && !before.equals(slots)) {
            changed.add(origin);
          }
        });
    slotCounts.clear();
    slotCounts.putAll(current);
    return changed;
  }

  public void clear() {
    slotCounts.clear();
  }

  private static Map<StorageOrigin, OptionalInt> slotCountsOf(ContainerScanResult scan) {
    final Map<StorageOrigin, OptionalInt> counts = new LinkedHashMap<>();
    for (final StorageProvider provider : scan.providers()) {
      counts.put(provider.origin(), provider.slotCount());
    }
    return counts;
  }
}
