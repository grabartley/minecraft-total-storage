package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.Optional;
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
                  "Stored %s, with no room for the other %s",
                  outcome.moved(),
                  outcome.remaining()));
      case NOTHING_TO_DEPOSIT -> Optional.of(line("deposit.empty", "There is nothing to store"));
      case SLOT_LOCKED ->
          Optional.of(line("deposit.locked", "That slot is locked, so nothing left it"));
      case NO_SPACE -> Optional.of(line("deposit.full", "This network has no room left"));
      case REFUSED -> Optional.of(line("refused", "That terminal is not yours to use right now"));
    };
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
