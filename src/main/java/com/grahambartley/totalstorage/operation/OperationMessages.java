package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.text.CountedNoun;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class OperationMessages {
  private static final String PREFIX = "operation." + TotalStorage.MOD_ID + ".";

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
      case NO_SPACE ->
          Optional.of(
              line(
                  "deposit.full",
                  "No room for %s, the network is full",
                  leftovers(outcome.leftovers())));
      case ONLY_FILTERED_SPACE ->
          Optional.of(
              line(
                  "deposit.filtered_only",
                  "No room for %s, the free space only takes filtered items",
                  leftovers(outcome.leftovers())));
      case NO_FITTING_SPACE ->
          Optional.of(
              line(
                  "deposit.unfitting",
                  "No room for %s, the free slots will not hold them",
                  leftovers(outcome.leftovers())));
      case RATE_LIMITED -> Optional.empty();
      case REFUSED -> Optional.of(line("refused", "That terminal is not yours to use right now"));
    };
  }

  public static TerminalNoticeTone toneOf(WithdrawResult result) {
    return switch (result) {
      case WITHDRAWN -> TerminalNoticeTone.DONE;
      case PARTIAL_NO_STOCK, PARTIAL_NO_ROOM -> TerminalNoticeTone.PARTIAL;
      case NOTHING_AVAILABLE, NO_ROOM, REFUSED -> TerminalNoticeTone.FAILED;
    };
  }

  public static Optional<Text> headlineForWithdrawal(WithdrawOutcome outcome) {
    return switch (outcome.result()) {
      case PARTIAL_NO_STOCK, PARTIAL_NO_ROOM ->
          Optional.of(
              line(
                  "withdraw.partial.headline",
                  "Took %s of %s",
                  outcome.moved(),
                  outcome.requested()));
      case NOTHING_AVAILABLE -> Optional.of(line("withdraw.gone.headline", "That item is gone"));
      case NO_ROOM -> Optional.of(line("withdraw.full.headline", "Your inventory is full"));
      case WITHDRAWN, REFUSED -> forWithdrawal(outcome);
    };
  }

  public static TerminalNoticeTone toneOf(DepositResult result) {
    return switch (result) {
      case DEPOSITED, RATE_LIMITED -> TerminalNoticeTone.DONE;
      case PARTIAL, NOTHING_TO_DEPOSIT -> TerminalNoticeTone.PARTIAL;
      case NO_SPACE, ONLY_FILTERED_SPACE, NO_FITTING_SPACE, REFUSED -> TerminalNoticeTone.FAILED;
    };
  }

  public static Optional<Text> headlineForDeposit(DepositOutcome outcome) {
    return switch (outcome.result()) {
      case PARTIAL ->
          Optional.of(
              line("deposit.partial.headline", "%s did not fit", leftovers(outcome.leftovers())));
      case NO_SPACE -> Optional.of(line("deposit.full.headline", "Network full, nothing stored"));
      case ONLY_FILTERED_SPACE ->
          Optional.of(line("deposit.filtered_only.headline", "Only filtered space is left"));
      case NO_FITTING_SPACE ->
          Optional.of(line("deposit.unfitting.headline", "No free slot will hold these"));
      case DEPOSITED, NOTHING_TO_DEPOSIT, RATE_LIMITED, REFUSED -> forDeposit(outcome);
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
