package com.grahambartley.totalstorage.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;

public final class DataComponentKey {
  private static final String NONE = "";
  private static final String ENTRY_SEPARATOR = ";";
  private static final String VALUE_SEPARATOR = "=";
  private static final String REMOVED = "-";

  private DataComponentKey() {}

  public static String of(ComponentChanges changes) {
    if (changes == null || changes.isEmpty()) {
      return NONE;
    }
    final List<String> entries = new ArrayList<>(changes.size());
    for (final Map.Entry<ComponentType<?>, Optional<?>> change : changes.entrySet()) {
      entries.add(encode(change.getKey(), change.getValue()));
    }
    return String.join(ENTRY_SEPARATOR, entries.stream().sorted().toList());
  }

  private static String encode(ComponentType<?> type, Optional<?> value) {
    final String id = String.valueOf(Registries.DATA_COMPONENT_TYPE.getId(type));
    return id + VALUE_SEPARATOR + value.map(String::valueOf).orElse(REMOVED);
  }
}
