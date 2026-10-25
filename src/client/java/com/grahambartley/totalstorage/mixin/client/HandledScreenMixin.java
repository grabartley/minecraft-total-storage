package com.grahambartley.totalstorage.mixin.client;

import com.grahambartley.totalstorage.lock.LockClicks;
import com.grahambartley.totalstorage.lock.LockGesture;
import com.grahambartley.totalstorage.lock.LockHintRing;
import com.grahambartley.totalstorage.lock.LockTooltipLines;
import com.grahambartley.totalstorage.lock.LockedSlotBadge;
import com.grahambartley.totalstorage.lock.LockedSlotOverlay;
import com.grahambartley.totalstorage.screen.ModKeyBindings;
import com.grahambartley.totalstorage.screen.QuickStackRequests;
import com.grahambartley.totalstorage.screen.ScreenKeyboardFocus;
import com.grahambartley.totalstorage.screen.SortRequests;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
  @Shadow protected Slot focusedSlot;

  @Shadow
  protected abstract Slot getSlotAt(double mouseX, double mouseY);

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void totalStorage$toggleLockInsteadOfMovingItems(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callback) {
    if (!LockGesture.isLockToggle(button, Screen.hasAltDown())) {
      return;
    }
    LockClicks.requestToggle(getSlotAt(mouseX, mouseY));
    callback.setReturnValue(true);
  }

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void totalStorage$quickStackFromTheInventory(
      int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
    if (!((Object) this instanceof InventoryScreen)
        || !ModKeyBindings.isQuickStack(keyCode, scanCode)) {
      return;
    }
    QuickStackRequests.send();
    callback.setReturnValue(true);
  }

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void totalStorage$sortTheScreenInFront(
      int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
    if (!ModKeyBindings.isSort(keyCode, scanCode)
        || ScreenKeyboardFocus.isTyping(((HandledScreen<?>) (Object) this).getFocused())) {
      return;
    }
    if (SortRequests.send(((HandledScreen<?>) (Object) this).getScreenHandler().syncId)) {
      callback.setReturnValue(true);
    }
  }

  @Inject(method = "drawSlot", at = @At("HEAD"))
  private void totalStorage$paintLockedSlots(
      DrawContext context, Slot slot, CallbackInfo callback) {
    LockedSlotOverlay.draw(context, slot);
  }

  @Inject(method = "drawSlot", at = @At("TAIL"))
  private void totalStorage$badgeLockedSlots(
      DrawContext context, Slot slot, CallbackInfo callback) {
    LockedSlotBadge.draw(context, slot);
    LockHintRing.draw(context, slot, Screen.hasAltDown());
  }

  @ModifyArg(
      method = "drawMouseoverTooltip",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"),
      index = 1)
  private List<Text> totalStorage$explainALockInText(List<Text> lines) {
    if (!LockTooltipLines.applies(focusedSlot, Screen.hasAltDown())) {
      return lines;
    }
    return LockTooltipLines.withText(lines, focusedSlot.getStack().getTooltipData().isPresent());
  }

  @ModifyArg(
      method = "drawMouseoverTooltip",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"),
      index = 2)
  private Optional<TooltipData> totalStorage$explainALock(Optional<TooltipData> data) {
    if (!LockTooltipLines.applies(focusedSlot, Screen.hasAltDown())) {
      return data;
    }
    return LockTooltipLines.withHints(data, MinecraftClient.IS_SYSTEM_MAC);
  }
}
