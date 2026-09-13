package com.grahambartley.toomanychests.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
  @Accessor("x")
  int tooManyChests$panelX();

  @Accessor("y")
  int tooManyChests$panelY();

  @Accessor("backgroundWidth")
  int tooManyChests$panelWidth();

  @Accessor("backgroundHeight")
  int tooManyChests$panelHeight();
}
