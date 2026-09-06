package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * What a {@code FILTERED} container has declared it accepts: the tags it takes, and the individual
 * items it takes regardless of tag. See ADR 0004.
 *
 * <p>Membership is by tag identifier and item identifier rather than by {@code ItemVariant}, the
 * same choice {@code NetworkTag} makes, so an enchanted diamond sword is accepted wherever a plain
 * one is.
 *
 * <p>An empty filter accepts nothing rather than everything. A container that has declared what it
 * takes and named nothing is the safe direction to be wrong in: routing sends items elsewhere
 * rather than into a container whose owner has not finished configuring it.
 *
 * <p>Both sets are sorted rather than kept in the order edits arrived, so two containers configured
 * the same way compare equal and encode identically.
 *
 * @param tagIds the network tag ids this container accepts, normalised
 * @param itemIds the item ids this container accepts outright, normalised
 */
public record ContainerFilter(Set<String> tagIds, Set<String> itemIds) {
  public static final ContainerFilter NONE = new ContainerFilter(Set.of(), Set.of());

  public ContainerFilter {
    tagIds = canonicalise(tagIds);
    itemIds = canonicalise(itemIds);
  }

  /** Builds from whatever collections a save or a payload hands over, duplicates included. */
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

  /**
   * Whether this container takes an item, given the tags the network files that item under. The
   * caller resolves those tags because preset membership is computed rather than stored; see ADR
   * 0011.
   */
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
