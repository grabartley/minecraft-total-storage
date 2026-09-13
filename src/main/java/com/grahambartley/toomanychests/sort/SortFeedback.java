package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.command.CommandMessages;
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

  static Text messageFor(SortOutcome outcome) {
    return switch (outcome) {
      case SORTED -> CommandMessages.sorted();
      case ALREADY_SORTED -> CommandMessages.alreadySorted();
      case NOTHING_TO_SORT -> CommandMessages.nothingToSort();
      case FAILED -> CommandMessages.sortFailed();
      case NO_OPEN_SCREEN, SCREEN_NOT_SORTABLE -> CommandMessages.nothingOpenToSort();
    };
  }
}
