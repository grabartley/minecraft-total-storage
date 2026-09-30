package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalGridRequests {
  private TerminalGridRequests() {}

  public static void send(GridAction action, int syncId, ListingRow<ItemVariant> row) {
    switch (action) {
      case WITHDRAW_STACK_TO_CURSOR ->
          withdraw(syncId, row, stackFrom(row), WithdrawDestination.CURSOR);
      case WITHDRAW_HALF_TO_CURSOR ->
          withdraw(syncId, row, halfStackFrom(row), WithdrawDestination.CURSOR);
      case WITHDRAW_STACK_TO_INVENTORY ->
          withdraw(syncId, row, stackFrom(row), WithdrawDestination.INVENTORY);
      case DEPOSIT_CURSOR_STACK ->
          ModNetworkingClient.sendDepositRequest(DepositRequestPayload.wholeCursor(syncId));
      case DEPOSIT_ONE_FROM_CURSOR ->
          ModNetworkingClient.sendDepositRequest(DepositRequestPayload.oneFromCursor(syncId));
      case PROMPT_FOR_AMOUNT, OPEN_TAGGING, NONE -> {}
    }
  }

  private static void withdraw(
      int syncId, ListingRow<ItemVariant> row, int amount, WithdrawDestination destination) {
    if (row != null && !row.variant().isBlank() && amount > 0) {
      ModNetworkingClient.sendWithdrawRequest(syncId, row.variant(), amount, destination);
    }
  }

  private static int stackFrom(ListingRow<ItemVariant> row) {
    return row == null || row.variant().isBlank()
        ? 0
        : WithdrawAmount.stackFrom(row.variant().toStack().getMaxCount(), row.count());
  }

  private static int halfStackFrom(ListingRow<ItemVariant> row) {
    return row == null || row.variant().isBlank()
        ? 0
        : WithdrawAmount.halfStackFrom(row.variant().toStack().getMaxCount(), row.count());
  }
}
