package com.grahambartley.totalstorage.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
  @Accessor("x")
  int totalStorage$panelX();

  @Accessor("y")
  int totalStorage$panelY();

  @Accessor("backgroundWidth")
  int totalStorage$panelWidth();

  @Accessor("backgroundHeight")
  int totalStorage$panelHeight();
}
