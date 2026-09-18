package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public record NetworkTag(String id, String displayName, boolean immutable, Set<String> items) {
  public NetworkTag {
    id = TagNameRule.idFor(id);
    if (id.isEmpty()) {
      throw new IllegalArgumentException("tag id must not be blank");
    }
    displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
    items = sanitizeItems(items);
  }

  public static NetworkTag preset(PresetTag preset) {
    return new NetworkTag(preset.id(), preset.translationKey(), true, Set.of());
  }

  public static NetworkTag custom(String id) {
    return custom(id, id, Set.of());
  }

  public static NetworkTag custom(String id, String displayName) {
    return custom(id, displayName, Set.of());
  }

  public static NetworkTag custom(String id, String displayName, Collection<String> items) {
    return new NetworkTag(id, displayName, false, sanitizeItems(items));
  }

  public boolean contains(String itemId) {
    return items.contains(FilterText.normalise(itemId));
  }

  public NetworkTag withItem(String itemId) {
    final String item = FilterText.normalise(itemId);
    if (immutable || item.isEmpty() || items.contains(item)) {
      return this;
    }
    final Set<String> next = new TreeSet<>(items);
    next.add(item);
    return new NetworkTag(id, displayName, false, next);
  }

  public NetworkTag withoutItem(String itemId) {
    if (immutable || !contains(itemId)) {
      return this;
    }
    final Set<String> next = new TreeSet<>(items);
    next.remove(FilterText.normalise(itemId));
    return new NetworkTag(id, displayName, false, next);
  }

  public NetworkTag withDisplayName(String name) {
    if (immutable || name == null || name.isBlank() || name.trim().equals(displayName)) {
      return this;
    }
    return new NetworkTag(id, name, false, items);
  }

  private static Set<String> sanitizeItems(Collection<String> raw) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<String> kept = new TreeSet<>();
    for (final String item : raw) {
      final String normalised = FilterText.normalise(item);
      if (!normalised.isEmpty()) {
        kept.add(normalised);
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
