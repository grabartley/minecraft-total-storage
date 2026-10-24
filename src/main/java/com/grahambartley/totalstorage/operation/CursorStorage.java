package com.grahambartley.totalstorage.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public final class CursorStorage extends SingleStackStorage {
  private final ScreenHandler handler;

  public CursorStorage(ScreenHandler handler) {
    this.handler = Objects.requireNonNull(handler, "handler");
  }

  @Override
  protected ItemStack getStack() {
    return handler.getCursorStack();
  }

  @Override
  protected void setStack(ItemStack stack) {
    handler.setCursorStack(stack);
  }
}
