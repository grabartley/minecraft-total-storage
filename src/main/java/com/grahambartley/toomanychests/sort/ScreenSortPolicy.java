package com.grahambartley.toomanychests.sort;

import java.util.Set;
import net.minecraft.util.Identifier;

/**
 * Which screens may be rearranged at all, decided by screen handler type.
 *
 * <p>The vanilla screens listed here hold items whose slot is their meaning: a furnace's fuel slot
 * is not interchangeable with its input, an anvil's second slot is the sacrifice, and a crafting
 * grid's layout is the recipe. Reordering them is not a worse arrangement, it is a different
 * outcome, so they are refused outright.
 *
 * <p>This is only the first of two gates, which is why an unknown modded screen passes here. A deny
 * list cannot name a screen it has never heard of, so the structural check in {@link SortableSlots}
 * is what refuses unknown special-purpose screens: a machine whose slots restrict what they accept
 * fails that check whatever it is called. A modded chest that is genuinely a row of interchangeable
 * slots passes both, which is the point.
 */
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

  /** A screen with no registered type is refused, because nothing is known about its slots. */
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
