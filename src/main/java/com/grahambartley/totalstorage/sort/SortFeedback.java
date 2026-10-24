package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.command.CommandMessages;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class SortFeedback {
  private SortFeedback() {}

  public static void report(ServerPlayerEntity player, SortOutcome outcome) {
    if (player == null || outcome == null) {
      return;
    }
    player.sendMessage(messageFor(outcome), true);
  }

  public static Text messageFor(SortReply reply) {
    if (reply.subject() != SortSubject.INVENTORY) {
      return messageFor(reply.outcome());
    }
    return switch (reply.outcome()) {
      case SORTED -> CommandMessages.inventorySorted();
      case ALREADY_SORTED -> CommandMessages.inventoryAlreadySorted();
      case NOTHING_TO_SORT -> CommandMessages.inventoryNothingToSort();
      default -> messageFor(reply.outcome());
    };
  }

  public static Text messageFor(SortOutcome outcome) {
    return switch (outcome) {
      case SORTED -> CommandMessages.sorted();
      case ALREADY_SORTED -> CommandMessages.alreadySorted();
      case NOTHING_TO_SORT -> CommandMessages.nothingToSort();
      case FAILED -> CommandMessages.sortFailed();
      case RATE_LIMITED -> CommandMessages.sortTooFast();
      case NOT_OFFERED -> CommandMessages.sortButtonDisabled();
      case NO_OPEN_SCREEN, SCREEN_NOT_SORTABLE -> CommandMessages.sortRefused();
      case NOT_OPENABLE -> CommandMessages.notOpenableToSort();
    };
  }
}
