package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

/**
 * Turns a decided {@link GridAction} into the request that carries it.
 *
 * <p>Separate from the screen because the screen's job is drawing and hit testing, and because the
 * amounts a gesture stands for are the server's arithmetic rather than this end's: {@link
 * WithdrawAmount} is shared so a full stack means the same thing on both sides of the wire.
 *
 * <p>Nothing is predicted. The grid is not updated here and the cursor is not filled in; both wait
 * for the server's answer, because per ADR 0009 this end does not know whether the item is still
 * there.
 */
public final class TerminalGridRequests {

  private TerminalGridRequests() {}

  /** Sends what the action asks for, if it asks for anything the server decides. */
  public static void send(GridAction action, int syncId, ItemVariant variant) {
    switch (action) {
      case WITHDRAW_STACK_TO_CURSOR ->
          withdraw(syncId, variant, stackSize(variant), WithdrawDestination.CURSOR);
      case WITHDRAW_HALF_TO_CURSOR ->
          withdraw(
              syncId,
              variant,
              WithdrawAmount.halfStack(stackSize(variant)),
              WithdrawDestination.CURSOR);
      case WITHDRAW_STACK_TO_INVENTORY ->
          withdraw(syncId, variant, stackSize(variant), WithdrawDestination.INVENTORY);
      case DEPOSIT_CURSOR_STACK ->
          ModNetworkingClient.sendDepositRequest(DepositRequestPayload.wholeCursor(syncId));
      case DEPOSIT_ONE_FROM_CURSOR ->
          ModNetworkingClient.sendDepositRequest(DepositRequestPayload.oneFromCursor(syncId));
      case PROMPT_FOR_AMOUNT, NONE -> {}
    }
  }

  private static void withdraw(
      int syncId, ItemVariant variant, int amount, WithdrawDestination destination) {
    if (variant != null && !variant.isBlank() && amount > 0) {
      ModNetworkingClient.sendWithdrawRequest(syncId, variant, amount, destination);
    }
  }

  private static int stackSize(ItemVariant variant) {
    return variant == null || variant.isBlank()
        ? 0
        : WithdrawAmount.fullStack(variant.toStack().getMaxCount());
  }
}
