package com.grahambartley.toomanychests.mixin;

import com.grahambartley.toomanychests.sort.MiddleClickSort;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
  @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
  private void tooManyChests$sortOnMiddleClick(
      int slotIndex,
      int button,
      SlotActionType actionType,
      PlayerEntity player,
      CallbackInfo callback) {
    if (MiddleClickSort.handle((ScreenHandler) (Object) this, actionType, player)) {
      callback.cancel();
    }
  }
}
