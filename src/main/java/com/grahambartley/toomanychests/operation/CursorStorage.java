package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

/**
 * The stack on the player's pointer, seen as somewhere items can be moved to and from.
 *
 * <p>The cursor is the one destination vanilla owns and the Transfer API does not, so a withdrawal
 * onto it would otherwise have to be written outside a transaction and hope. Wrapping it means the
 * cursor rolls back with everything else: a withdrawal that fails part way leaves the pointer
 * holding exactly what it held before.
 *
 * <p>The stack is read and written through the handler rather than copied, so nothing here can
 * disagree with what the player is looking at.
 */
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
