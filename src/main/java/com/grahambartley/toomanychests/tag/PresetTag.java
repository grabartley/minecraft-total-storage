package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The fixed set of tags every network ships with, so a freshly placed terminal has a populated
 * filter column with no setup. See ADR 0006. Presets are compiled in rather than authored per
 * network, which is what makes them impossible to rename or delete. Declaration order is the order
 * the filter column presents them in.
 */
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

  /** Where an item lands when it matches no other preset, so nothing is ever unreachable. */
  public static final PresetTag FALLBACK = MISCELLANEOUS;

  private static final List<PresetTag> ALL = List.of(values());

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  /**
   * Follows the convention tag translation key format, so recipe viewers can name a preset without
   * knowing anything about this mod.
   */
  public String translationKey() {
    return "tag.item." + TooManyChests.MOD_ID + ".preset." + id();
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
