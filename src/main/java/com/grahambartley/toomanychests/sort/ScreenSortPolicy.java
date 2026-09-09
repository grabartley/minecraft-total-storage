package com.grahambartley.toomanychests.sort;

import java.util.Set;
import net.minecraft.util.Identifier;

public final class ScreenSortPolicy {
  private static final Set<Identifier> DENIED =
      Set.of(
          vanilla("anvil"),
          vanilla("beacon"),
          vanilla("blast_furnace"),
          vanilla("brewing_stand"),
          vanilla("cartography_table"),
          vanilla("crafter_3x3"),
          vanilla("crafting"),
          vanilla("enchantment"),
          vanilla("furnace"),
          vanilla("grindstone"),
          vanilla("lectern"),
          vanilla("loom"),
          vanilla("merchant"),
          vanilla("smithing"),
          vanilla("smoker"),
          vanilla("stonecutter"));

  private ScreenSortPolicy() {}

  public static boolean allows(Identifier screenTypeId) {
    return screenTypeId != null && !DENIED.contains(screenTypeId);
  }

  public static Set<Identifier> deniedTypes() {
    return DENIED;
  }

  private static Identifier vanilla(String path) {
    return Identifier.ofVanilla(path);
  }
}
