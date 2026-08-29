package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.tag.PresetTagRules;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Everything the sort comparator needs to know about one holding, as plain data.
 *
 * <p>Per ADR 0012 this is what keeps the ordering testable: the game side flattens an item variant
 * and its count into these fields once, and nothing downstream needs a registry to decide where a
 * holding lands. The same candidate describes a slot in a container and a row in the terminal grid,
 * which is what lets the container sort, the grid, and consolidation share one comparator.
 *
 * @param categories every preset the item is filed under, normalised through {@link
 *     PresetTagRules}, so an item matching nothing still groups under the fallback
 * @param identifier the namespaced item identifier, which is the language independent sort key
 * @param componentsKey a stable encoding of the item's data components, so variants of one item
 *     cluster in a fixed order
 * @param displayName the item's translated name, used only under display name ordering
 * @param count how much of the item this holding carries
 * @param maxCount the item's stack limit, which is what makes a holding full or partial
 */
public record SortCandidate(
    Set<PresetTag> categories,
    String identifier,
    String componentsKey,
    String displayName,
    long count,
    int maxCount) {

  public SortCandidate {
    Objects.requireNonNull(identifier, "identifier");
    categories = PresetTagRules.assign(nonNull(categories));
    identifier = FilterText.normalise(identifier);
    if (identifier.isEmpty()) {
      throw new IllegalArgumentException("identifier must not be blank");
    }
    componentsKey = FilterText.normalise(componentsKey);
    displayName = FilterText.normalise(displayName);
    if (count < 0L) {
      throw new IllegalArgumentException("count must not be negative: " + count);
    }
    if (maxCount < 1) {
      throw new IllegalArgumentException("maxCount must be at least one: " + maxCount);
    }
  }

  /**
   * The one category the item groups under, which is its first preset in filter column order. An
   * item filed under several presets has to land in exactly one place in a sorted container, and
   * picking the earliest keeps that choice stable as presets are added.
   */
  public PresetTag primaryCategory() {
    for (final PresetTag preset : PresetTag.all()) {
      if (categories.contains(preset)) {
        return preset;
      }
    }
    return PresetTag.FALLBACK;
  }

  /**
   * The same holding at a different quantity, which is what merging two part stacks of one item
   * produces. The count is part of the ordering, so it cannot be changed without rebuilding.
   */
  public SortCandidate withCount(long newCount) {
    return newCount == count
        ? this
        : new SortCandidate(categories, identifier, componentsKey, displayName, newCount, maxCount);
  }

  /**
   * Lowered to whatever a destination will actually hold, so an arrangement fits where it lands.
   */
  public SortCandidate cappedAt(int slotCapacity) {
    final int capped = Math.min(maxCount, slotCapacity);
    return capped == maxCount
        ? this
        : new SortCandidate(categories, identifier, componentsKey, displayName, count, capped);
  }

  public boolean isFullStack() {
    return count >= maxCount;
  }

  private static Collection<PresetTag> nonNull(Collection<PresetTag> categories) {
    return categories == null ? Set.of() : categories;
  }
}
