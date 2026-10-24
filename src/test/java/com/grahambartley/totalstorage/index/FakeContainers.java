package com.grahambartley.totalstorage.index;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class FakeContainers implements Function<StorageOrigin, Map<String, Long>> {
  private final Map<StorageOrigin, Map<String, Long>> held = new LinkedHashMap<>();
  private final List<StorageOrigin> reads = new ArrayList<>();

  void put(StorageOrigin origin, String variant, long amount) {
    held.computeIfAbsent(origin, key -> new LinkedHashMap<>()).put(variant, amount);
  }

  void clear(StorageOrigin origin) {
    held.remove(origin);
  }

  @Override
  public Map<String, Long> apply(StorageOrigin origin) {
    reads.add(origin);
    return held.getOrDefault(origin, Map.of());
  }

  int readsOf(StorageOrigin origin) {
    return (int) reads.stream().filter(origin::equals).count();
  }

  int totalReads() {
    return reads.size();
  }
}
