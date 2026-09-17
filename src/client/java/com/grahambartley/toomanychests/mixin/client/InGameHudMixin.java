package com.grahambartley.toomanychests.mixin.client;

import com.grahambartley.toomanychests.lock.HotbarLockBadges;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
  @Inject(method = "renderHotbar", at = @At("TAIL"))
  private void tooManyChests$badgeLockedHotbarSlots(
      DrawContext context, RenderTickCounter tickCounter, CallbackInfo callback) {
    HotbarLockBadges.draw(context);
  }
}
