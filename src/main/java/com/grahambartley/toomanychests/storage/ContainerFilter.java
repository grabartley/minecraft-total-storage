package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.tag.TagNameRule;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

public record ContainerFilter(Set<String> tagIds, Set<String> itemIds) {
  public static final ContainerFilter NONE = new ContainerFilter(Set.of(), Set.of());

  public ContainerFilter {
    tagIds = canonicalise(tagIds, TagNameRule::idFor);
    itemIds = canonicalise(itemIds, FilterText::normalise);
  }

  public static ContainerFilter of(Collection<String> tagIds, Collection<String> itemIds) {
    return new ContainerFilter(asSet(tagIds), asSet(itemIds));
  }

  private static Set<String> asSet(Collection<String> raw) {
    return raw == null ? Set.of() : new LinkedHashSet<>(raw);
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
    return tagIds.contains(TagNameRule.idFor(tagId));
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
    return hasTag(tagId)
        ? this
        : new ContainerFilter(plus(tagIds, TagNameRule.idFor(tagId)), itemIds);
  }

  public ContainerFilter withoutTag(String tagId) {
    return hasTag(tagId)
        ? new ContainerFilter(minus(tagIds, TagNameRule.idFor(tagId)), itemIds)
        : this;
  }

  public ContainerFilter withItem(String itemId) {
    return hasItem(itemId)
        ? this
        : new ContainerFilter(tagIds, plus(itemIds, FilterText.normalise(itemId)));
  }

  public ContainerFilter withoutItem(String itemId) {
    return hasItem(itemId)
        ? new ContainerFilter(tagIds, minus(itemIds, FilterText.normalise(itemId)))
        : this;
  }

  private static Set<String> plus(Set<String> existing, String value) {
    final Set<String> next = new TreeSet<>(existing);
    next.add(value);
    return next;
  }

  private static Set<String> minus(Set<String> existing, String value) {
    final Set<String> next = new TreeSet<>(existing);
    next.remove(value);
    return next;
  }

  private static Set<String> canonicalise(Collection<String> raw, UnaryOperator<String> rule) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<String> kept = new TreeSet<>();
    for (final String value : raw) {
      final String canonical = rule.apply(value);
      if (!canonical.isEmpty()) {
        kept.add(canonical);
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
