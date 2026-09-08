package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * The keys the terminal screen answers to, so its actions are reachable without aiming at a button.
 *
 * <p>Registered as real key bindings rather than hard-coded keys, because a hard-coded one is
 * invisible in the controls screen and unfixable when it collides with something the player already
 * uses. Registering them also puts them in the conflict list, which is where a player expects to
 * discover a mod's keys.
 *
 * <p>They are read by the screen while it is open rather than polled every client tick. A
 * deposit-all that fired while the player was walking around would be a way to empty an inventory
 * by accident, and the terminal is the only place the gesture means anything.
 */
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

  /**
   * Whether a key press is the deposit-all key.
   *
   * <p>Compared against the binding's current key rather than against a constant, so rebinding it
   * in the controls screen rebinds it here too. An unregistered binding matches nothing, which is
   * what keeps a screen opened before registration from throwing.
   */
  public static boolean isDepositAll(int keyCode, int scanCode) {
    return depositAll != null && depositAll.matchesKey(keyCode, scanCode);
  }

  public static KeyBinding depositAll() {
    return depositAll;
  }
}
