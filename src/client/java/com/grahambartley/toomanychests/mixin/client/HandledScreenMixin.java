package com.grahambartley.toomanychests.mixin.client;

import com.grahambartley.toomanychests.lock.LockClicks;
import com.grahambartley.toomanychests.lock.LockGesture;
import com.grahambartley.toomanychests.lock.LockedSlotOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
  @Shadow
  protected abstract Slot getSlotAt(double mouseX, double mouseY);

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void tooManyChests$toggleLockInsteadOfMovingItems(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callback) {
    if (!LockGesture.isLockToggle(button, Screen.hasAltDown())) {
      return;
    }
    LockClicks.requestToggle(getSlotAt(mouseX, mouseY));
    callback.setReturnValue(true);
  }

  @Inject(method = "drawSlot", at = @At("HEAD"))
  private void tooManyChests$paintLockedSlots(
      DrawContext context, Slot slot, CallbackInfo callback) {
    LockedSlotOverlay.draw(context, slot);
  }
}
