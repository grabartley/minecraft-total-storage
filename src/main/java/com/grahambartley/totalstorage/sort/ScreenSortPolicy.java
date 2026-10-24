package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
          vanilla("stonecutter"),
          mod("terminal"));

  private ScreenSortPolicy() {}

  public static boolean allows(Identifier screenTypeId) {
    return screenTypeId != null && !DENIED.contains(screenTypeId);
  }

  public static boolean allowsHandler(ScreenHandler handler) {
    return handler != null && (handler instanceof PlayerScreenHandler || allows(typeIdOf(handler)));
  }

  @Nullable
  public static Identifier typeIdOf(ScreenHandler handler) {
    if (handler == null) {
      return null;
    }
    try {
      return Registries.SCREEN_HANDLER.getId(handler.getType());
    } catch (UnsupportedOperationException noType) {
      return null;
    }
  }

  public static Set<Identifier> deniedTypes() {
    return DENIED;
  }

  private static Identifier vanilla(String path) {
    return Identifier.ofVanilla(path);
  }

  private static Identifier mod(String path) {
    return Identifier.of(TotalStorage.MOD_ID, path);
  }
}
