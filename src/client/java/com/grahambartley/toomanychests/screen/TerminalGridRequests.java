package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalGridRequests {
  private TerminalGridRequests() {}

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
