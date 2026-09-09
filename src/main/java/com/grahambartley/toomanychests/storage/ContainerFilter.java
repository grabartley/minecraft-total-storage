package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record ContainerFilter(Set<String> tagIds, Set<String> itemIds) {
  public static final ContainerFilter NONE = new ContainerFilter(Set.of(), Set.of());

  public ContainerFilter {
    tagIds = canonicalise(tagIds);
    itemIds = canonicalise(itemIds);
  }

  public static ContainerFilter of(Collection<String> tagIds, Collection<String> itemIds) {
    return new ContainerFilter(canonicalise(tagIds), canonicalise(itemIds));
  }

  public static ContainerFilter ofTags(String... tagIds) {
    return of(List.of(tagIds), List.of());
  }

  public static ContainerFilter ofItems(String... itemIds) {
    return of(List.of(), List.of(itemIds));
  }

  public boolean isEmpty() {
    return tagIds.isEmpty() && itemIds.isEmpty();
  }

  public boolean hasTag(String tagId) {
    return tagIds.contains(FilterText.normalise(tagId));
  }

  public boolean hasItem(String itemId) {
    return itemIds.contains(FilterText.normalise(itemId));
  }

  public boolean accepts(String itemId, Collection<String> itemTagIds) {
    if (isEmpty()) {
      return false;
    }
    if (hasItem(itemId)) {
      return true;
    }
    if (tagIds.isEmpty() || itemTagIds == null) {
      return false;
    }
    return itemTagIds.stream().anyMatch(this::hasTag);
  }

  public ContainerFilter withTag(String tagId) {
    return hasTag(tagId) ? this : new ContainerFilter(plus(tagIds, tagId), itemIds);
  }

  public ContainerFilter withoutTag(String tagId) {
    return hasTag(tagId) ? new ContainerFilter(minus(tagIds, tagId), itemIds) : this;
  }

  public ContainerFilter withItem(String itemId) {
    return hasItem(itemId) ? this : new ContainerFilter(tagIds, plus(itemIds, itemId));
  }

  public ContainerFilter withoutItem(String itemId) {
    return hasItem(itemId) ? new ContainerFilter(tagIds, minus(itemIds, itemId)) : this;
  }

  private static Set<String> plus(Set<String> existing, String value) {
    final Set<String> next = new TreeSet<>(existing);
    next.add(FilterText.normalise(value));
    return next;
  }

  private static Set<String> minus(Set<String> existing, String value) {
    final Set<String> next = new TreeSet<>(existing);
    next.remove(FilterText.normalise(value));
    return next;
  }

  private static Set<String> canonicalise(Collection<String> raw) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<String> kept = new TreeSet<>();
    for (final String value : raw) {
      final String normalised = FilterText.normalise(value);
      if (!normalised.isEmpty()) {
        kept.add(normalised);
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
