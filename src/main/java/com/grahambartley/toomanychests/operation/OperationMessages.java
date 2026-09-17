package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.text.CountedNoun;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class OperationMessages {
  private static final String PREFIX = "operation." + TooManyChests.MOD_ID + ".";

  private static final int NAMED_LEFTOVERS = 2;

  private OperationMessages() {}

  public static Optional<Text> forWithdrawal(WithdrawOutcome outcome) {
    return switch (outcome.result()) {
      case WITHDRAWN -> Optional.empty();
      case PARTIAL_NO_STOCK ->
          Optional.of(
              line(
                  "withdraw.partial.stock",
                  "Took %s of %s, that was all the network had",
                  outcome.moved(),
                  outcome.requested()));
      case PARTIAL_NO_ROOM ->
          Optional.of(
              line(
                  "withdraw.partial.room",
                  "Took %s of %s, the rest would not fit",
                  outcome.moved(),
                  outcome.requested()));
      case NOTHING_AVAILABLE ->
          Optional.of(line("withdraw.gone", "The network no longer holds that"));
      case NO_ROOM -> Optional.of(line("withdraw.full", "You have nowhere to put that"));
      case REFUSED -> Optional.of(line("refused", "That terminal is not yours to use right now"));
    };
  }

  public static Optional<Text> forDeposit(DepositOutcome outcome) {
    return switch (outcome.result()) {
      case DEPOSITED -> Optional.empty();
      case PARTIAL ->
          Optional.of(
              line(
                  "deposit.partial",
                  "Stored %s, with no room for %s",
                  CountedNoun.ITEMS.of(outcome.moved()),
                  leftovers(outcome.leftovers())));
      case NOTHING_TO_DEPOSIT -> Optional.of(line("deposit.empty", "There is nothing to store"));
      case SLOT_LOCKED ->
          Optional.of(line("deposit.locked", "That slot is locked, so nothing left it"));
      case NO_SPACE -> Optional.of(line("deposit.full", "This network has no room left"));
      case RATE_LIMITED -> Optional.empty();
      case REFUSED -> Optional.of(line("refused", "That terminal is not yours to use right now"));
    };
  }

  public static Text rateLimited(ExpensiveOperation operation, long ticksLeft) {
    return line(
        "cooldown." + operation.id(), waitingOn(operation), Math.max(1L, (ticksLeft + 19L) / 20L));
  }

  private static String waitingOn(ExpensiveOperation operation) {
    return switch (operation) {
      case RESCAN -> "Give the last scan a moment, %ss left";
      case SORT_CONTAINER, SORT_NETWORK -> "Give the last sort a moment, %ss left";
      case CONSOLIDATE -> "Give the last consolidation a moment, %ss left";
      case QUICK_STACK -> "Give the last quick stack a moment, %ss left";
      case DEPOSIT_ALL, ASSIGN_MATCHING -> "Give the last deposit a moment, %ss left";
    };
  }

  private static Text leftovers(List<DepositLeftover> leftovers) {
    final MutableText named = Text.empty();
    for (int index = 0; index < Math.min(NAMED_LEFTOVERS, leftovers.size()); index++) {
      if (index > 0) {
        named.append(line("deposit.leftover.separator", ", "));
      }
      final DepositLeftover left = leftovers.get(index);
      named.append(
          line(
              "deposit.leftover.item", "%s %s", left.amount(), left.variant().getItem().getName()));
    }
    final int rest = leftovers.size() - Math.min(NAMED_LEFTOVERS, leftovers.size());
    if (rest <= 0) {
      return named;
    }
    return rest == 1
        ? line("deposit.leftover.more.one", "%s and 1 other kind", named)
        : line("deposit.leftover.more", "%s and %s other kinds", named, rest);
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
