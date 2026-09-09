package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public record NetworkTagSet(List<NetworkTag> all) {
  private static final NetworkTagSet DEFAULTS = new NetworkTagSet(List.of());

  public NetworkTagSet {
    all = canonicalise(all);
  }

  public static NetworkTagSet defaults() {
    return DEFAULTS;
  }

  public List<NetworkTag> custom() {
    return all.stream().filter(tag -> !tag.immutable()).toList();
  }

  public Optional<NetworkTag> byId(String id) {
    final String key = FilterText.normalise(id);
    return all.stream().filter(tag -> tag.id().equals(key)).findFirst();
  }

  public boolean contains(String id) {
    return byId(id).isPresent();
  }

  public NetworkTagSet withTag(NetworkTag tag) {
    if (tag == null || tag.immutable() || isPresetId(tag.id())) {
      return this;
    }
    if (byId(tag.id()).map(tag::equals).orElse(false)) {
      return this;
    }
    final List<NetworkTag> next = new ArrayList<>(custom());
    next.removeIf(existing -> existing.id().equals(tag.id()));
    next.add(tag);
    return new NetworkTagSet(next);
  }

  public NetworkTagSet withCustomTag(String id) {
    return withCustomTag(id, id);
  }

  public NetworkTagSet withCustomTag(String id, String displayName) {
    final String key = FilterText.normalise(id);
    if (key.isEmpty() || contains(key)) {
      return this;
    }
    return withTag(NetworkTag.custom(key, displayName));
  }

  public NetworkTagSet withoutTag(String id) {
    final String key = FilterText.normalise(id);
    if (byId(key).map(NetworkTag::immutable).orElse(true)) {
      return this;
    }
    final List<NetworkTag> next = new ArrayList<>(custom());
    next.removeIf(existing -> existing.id().equals(key));
    return new NetworkTagSet(next);
  }

  public NetworkTagSet withItem(String tagId, String itemId) {
    return replace(tagId, tag -> tag.withItem(itemId));
  }

  public NetworkTagSet withoutItem(String tagId, String itemId) {
    return replace(tagId, tag -> tag.withoutItem(itemId));
  }

  public Set<String> tagIdsFor(String itemId) {
    final String item = FilterText.normalise(itemId);
    if (item.isEmpty()) {
      return Set.of();
    }
    final Set<String> matched = new LinkedHashSet<>();
    for (final NetworkTag tag : all) {
      if (tag.contains(item)) {
        matched.add(tag.id());
      }
    }
    return Collections.unmodifiableSet(matched);
  }

  private NetworkTagSet replace(String tagId, UnaryOperator<NetworkTag> change) {
    final Optional<NetworkTag> existing = byId(tagId).filter(tag -> !tag.immutable());
    if (existing.isEmpty()) {
      return this;
    }
    return withTag(change.apply(existing.get()));
  }

  private static boolean isPresetId(String id) {
    return PresetTag.fromId(id).isPresent();
  }

  private static List<NetworkTag> canonicalise(Collection<NetworkTag> raw) {
    final Map<String, NetworkTag> presets = new LinkedHashMap<>();
    for (final PresetTag preset : PresetTag.all()) {
      presets.put(preset.id(), NetworkTag.preset(preset));
    }
    final Map<String, NetworkTag> customs = new TreeMap<>();
    if (raw != null) {
      for (final NetworkTag tag : raw) {
        if (tag != null && !tag.immutable() && !presets.containsKey(tag.id())) {
          customs.put(tag.id(), tag);
        }
      }
    }
    final List<NetworkTag> canonical = new ArrayList<>(presets.size() + customs.size());
    canonical.addAll(presets.values());
    canonical.addAll(customs.values());
    return List.copyOf(canonical);
  }
}
