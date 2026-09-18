package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ModKeyBindings {
  private static final String CATEGORY = "key.categories." + TooManyChests.MOD_ID;

  private static KeyBinding depositAll;
  private static KeyBinding quickStack;
  private static KeyBinding sort;

  private ModKeyBindings() {}

  public static void register() {
    depositAll = bind("deposit_all", GLFW.GLFW_KEY_R);
    quickStack = bind("quick_stack", GLFW.GLFW_KEY_V);
    sort = bind("sort", GLFW.GLFW_KEY_B);
  }

  public static KeyBinding quickStack() {
    return quickStack;
  }

  public static KeyBinding sort() {
    return sort;
  }

  public static boolean isDepositAll(int keyCode, int scanCode) {
    return depositAll != null && depositAll.matchesKey(keyCode, scanCode);
  }

  public static boolean isQuickStack(int keyCode, int scanCode) {
    return quickStack != null && quickStack.matchesKey(keyCode, scanCode);
  }

  public static boolean isSort(int keyCode, int scanCode) {
    return sort != null && sort.matchesKey(keyCode, scanCode);
  }

  private static KeyBinding bind(String name, int key) {
    return KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "key." + TooManyChests.MOD_ID + "." + name, InputUtil.Type.KEYSYM, key, CATEGORY));
  }
}
