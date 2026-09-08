package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * What the terminal tells a player about the items they just moved, in one place.
 *
 * <p>An outcome nobody is told about is indistinguishable from a mod that ignored the click, which
 * is the failure mode a stale listing makes most likely. Each line carries its English alongside
 * its translation key, so a server whose resource pack has not caught up still says words.
 *
 * <p>A whole withdrawal and a whole deposit say nothing. The items appearing is the feedback, and a
 * line for every successful click would be noise a player learns to ignore before the one line that
 * mattered arrives.
 */
public final class OperationMessages {
  private static final String PREFIX = "operation." + TooManyChests.MOD_ID + ".";

  /**
   * How many kinds of leftover are named before the line summarises instead.
   *
   * <p>A partial deposit-all can leave a dozen kinds behind, and a line naming all of them is one
   * nobody finishes reading. Naming the largest two and counting the rest keeps the sentence the
   * length of a sentence while still leading with what a player most wants to hear.
   */
  private static final int NAMED_LEFTOVERS = 2;

  private OperationMessages() {}

  public static Optional<Text> forWithdrawal(WithdrawOutcome outcome) {
    return switch (outcome.result()) {
      case WITHDRAWN -> Optional.empty();
      case PARTIAL ->
          Optional.of(
              line(
                  "withdraw.partial",
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
                  outcome.moved(),
                  leftovers(outcome.leftovers())));
      case NOTHING_TO_DEPOSIT -> Optional.of(line("deposit.empty", "There is nothing to store"));
      case SLOT_LOCKED ->
          Optional.of(line("deposit.locked", "That slot is locked, so nothing left it"));
      case NO_SPACE -> Optional.of(line("deposit.full", "This network has no room left"));
      case RATE_LIMITED -> Optional.empty();
      case REFUSED -> Optional.of(line("refused", "That terminal is not yours to use right now"));
    };
  }

  /**
   * What a player is told when they asked again too soon. Says the operation by name, so somebody
   * running two of them in quick succession learns which one is waiting rather than that "an"
   * operation is.
   */
  public static Text rateLimited(ExpensiveOperation operation, long ticksLeft) {
    return line(
        "cooldown." + operation.id(),
        operation == ExpensiveOperation.RESCAN
            ? "Give the last scan a moment, %ss left"
            : "Give the last deposit a moment, %ss left",
        Math.max(1L, (ticksLeft + 19L) / 20L));
  }

  /**
   * Names what stayed behind, because a bare total does not say which chest filled up.
   *
   * <p>Only ever called for a partial deposit, whose shortfall {@link DepositOutcome} requires to
   * be accounted for, so there is always at least one kind to name.
   */
  static Text leftovers(List<DepositLeftover> leftovers) {
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
    return rest <= 0 ? named : line("deposit.leftover.more", "%s and %s other kinds", named, rest);
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
