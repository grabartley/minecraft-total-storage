package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TerminalKeyBindings {
  private static final String CATEGORY = "key.categories." + TooManyChests.MOD_ID;

  private static KeyBinding depositAll;

  private TerminalKeyBindings() {}

  public static void register() {
    depositAll =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key." + TooManyChests.MOD_ID + ".deposit_all",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY));
  }

  public static boolean isDepositAll(int keyCode, int scanCode) {
    return depositAll != null && depositAll.matchesKey(keyCode, scanCode);
  }
}
