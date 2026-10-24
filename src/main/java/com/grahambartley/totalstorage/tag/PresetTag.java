package com.grahambartley.totalstorage.tag;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum PresetTag {
  BUILDING_BLOCKS,
  WOOD,
  STONE,
  ORES_AND_MINERALS,
  TOOLS,
  WEAPONS_AND_ARMOUR,
  FOOD,
  REDSTONE,
  DECORATION,
  NATURE,
  MOB_DROPS,
  BREWING,
  MISCELLANEOUS;

  public static final PresetTag FALLBACK = MISCELLANEOUS;

  private static final List<PresetTag> ALL = List.of(values());

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "tag.item." + TotalStorage.MOD_ID + ".preset." + id();
  }

  public static List<PresetTag> all() {
    return ALL;
  }

  public static Optional<PresetTag> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    String trimmed = id.trim();
    for (PresetTag preset : ALL) {
      if (preset.id().equalsIgnoreCase(trimmed)) {
        return Optional.of(preset);
      }
    }
    return Optional.empty();
  }
}
