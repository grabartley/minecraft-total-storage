package com.grahambartley.toomanychests;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

  public static final SoundEvent TERMINAL_POWER_ON = registerSound("block.terminal.power_on");
  public static final SoundEvent TERMINAL_POWER_OFF = registerSound("block.terminal.power_off");
  public static final SoundEvent TERMINAL_SCREEN_OPEN = registerSound("block.terminal.screen_open");
  public static final SoundEvent TERMINAL_DEPOSIT_CONFIRM =
      registerSound("block.terminal.deposit_confirm");

  private static SoundEvent registerSound(final String name) {
    final Identifier id = Identifier.of(TooManyChests.MOD_ID, name);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }

  public static void initialize() {}
}
