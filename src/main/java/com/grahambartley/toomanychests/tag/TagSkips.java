package com.grahambartley.toomanychests.tag;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record TagSkips(Map<TagSkipReason, Integer> byReason) {
  public static final TagSkips NONE = new TagSkips(Map.of());

  public TagSkips {
    byReason = tidied(byReason);
  }

  public static TagSkips of(TagSkipReason reason, int count) {
    return NONE.and(reason, count);
  }

  public TagSkips and(TagSkipReason reason) {
    return and(reason, 1);
  }

  public TagSkips and(TagSkipReason reason, int count) {
    if (reason == null || count <= 0) {
      return this;
    }
    final EnumMap<TagSkipReason, Integer> grown = new EnumMap<>(TagSkipReason.class);
    grown.putAll(byReason);
    grown.merge(reason, count, Integer::sum);
    return new TagSkips(grown);
  }

  public int total() {
    return byReason.values().stream().mapToInt(Integer::intValue).sum();
  }

  public int countOf(TagSkipReason reason) {
    return byReason.getOrDefault(reason, 0);
  }

  public boolean isEmpty() {
    return byReason.isEmpty();
  }

  private static Map<TagSkipReason, Integer> tidied(Map<TagSkipReason, Integer> raw) {
    if (raw == null || raw.isEmpty()) {
      return Map.of();
    }
    final EnumMap<TagSkipReason, Integer> kept = new EnumMap<>(TagSkipReason.class);
    for (final Map.Entry<TagSkipReason, Integer> entry : raw.entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
        kept.put(entry.getKey(), entry.getValue());
      }
    }
    return Collections.unmodifiableMap(kept);
  }
}
