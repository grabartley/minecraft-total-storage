package com.grahambartley.totalstorage.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public final class PromptRefusal {
  private static final float PITCH = 0.5F;

  private PromptRefusal() {}

  public static void sound() {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null || client.getSoundManager() == null) {
      return;
    }
    client
        .getSoundManager()
        .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, PITCH));
  }
}
