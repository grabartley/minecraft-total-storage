package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.tag.PresetTagRules;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

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

  public PresetTag primaryCategory() {
    for (final PresetTag preset : PresetTag.all()) {
      if (categories.contains(preset)) {
        return preset;
      }
    }
    return PresetTag.FALLBACK;
  }

  public SortCandidate withCount(long newCount) {
    return newCount == count
        ? this
        : new SortCandidate(categories, identifier, componentsKey, displayName, newCount, maxCount);
  }

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
