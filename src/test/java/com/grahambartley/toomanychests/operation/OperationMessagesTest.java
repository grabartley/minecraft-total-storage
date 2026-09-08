package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * What the terminal says after every move.
 *
 * <p>The keys are asserted rather than the English, because the English is a fallback for a client
 * whose resource pack has not caught up and the key is what a translator binds to. A branch that
 * quietly reused another's key would say the wrong thing in every language but this one.
 */
class OperationMessagesTest {
  private static final String PREFIX = "operation.too-many-chests.";

  private static String keyOf(Optional<Text> line) {
    assertTrue(line.isPresent(), "expected a message");
    return ((TranslatableTextContent) line.get().getContent()).getKey();
  }

  private static Object[] argumentsOf(Optional<Text> line) {
    return ((TranslatableTextContent) line.get().getContent()).getArgs();
  }

  /** Success says nothing: the items arriving is the feedback, and a line per click is noise. */
  @Test
  void aWholeWithdrawalAndAWholeDepositSayNothing() {
    assertTrue(OperationMessages.forWithdrawal(WithdrawOutcome.moved(64L, 64L)).isEmpty());
    assertTrue(OperationMessages.forDeposit(DepositOutcome.stored(64L, 64L)).isEmpty());
  }

  @Test
  void aPartialWithdrawalNamesBothWhatArrivedAndWhatWasAskedFor() {
    Optional<Text> line = OperationMessages.forWithdrawal(WithdrawOutcome.moved(64L, 9L));

    assertEquals(PREFIX + "withdraw.partial", keyOf(line));
    assertEquals(9L, argumentsOf(line)[0]);
    assertEquals(64L, argumentsOf(line)[1]);
  }

  @Test
  void aPartialDepositNamesWhatWasStoredAndWhatIsStillInHand() {
    Optional<Text> line = OperationMessages.forDeposit(DepositOutcome.stored(64L, 20L));

    assertEquals(PREFIX + "deposit.partial", keyOf(line));
    assertEquals(20L, argumentsOf(line)[0]);
    assertEquals(44L, amountIn(argumentsOf(line)[1]));
  }

  /**
   * A deposit that knew no items still says how much stayed behind, so the count never goes missing
   * when the leftovers could not be named.
   */
  @Test
  void aPartialDepositWithNoNamedLeftoversStillReportsTheAmount() {
    Text left = OperationMessages.leftovers(List.of(), 44L);

    assertEquals(
        PREFIX + "deposit.leftover.amount", ((TranslatableTextContent) left.getContent()).getKey());
    assertEquals(44L, ((TranslatableTextContent) left.getContent()).getArgs()[0]);
  }

  /** The amount is nested inside the leftovers line, which names items when it knows any. */
  private static long amountIn(Object argument) {
    return (long) ((TranslatableTextContent) ((Text) argument).getContent()).getArgs()[0];
  }

  /** The three empty-handed endings are the ones a player cannot tell apart without being told. */
  @Test
  void everyEmptyHandedEndingGetsAReasonOfItsOwn() {
    assertEquals(
        PREFIX + "withdraw.gone",
        keyOf(OperationMessages.forWithdrawal(WithdrawOutcome.nothingAvailable(64L))));
    assertEquals(
        PREFIX + "withdraw.full",
        keyOf(OperationMessages.forWithdrawal(WithdrawOutcome.noRoom(64L))));
    assertEquals(
        PREFIX + "deposit.empty",
        keyOf(OperationMessages.forDeposit(DepositOutcome.nothingToDeposit())));
    assertEquals(
        PREFIX + "deposit.full", keyOf(OperationMessages.forDeposit(DepositOutcome.noSpace(64L))));
    assertEquals(
        PREFIX + "deposit.locked",
        keyOf(OperationMessages.forDeposit(DepositOutcome.slotLocked())));
  }

  /** A lock that reports itself as an empty slot teaches a player to distrust the lock. */
  @Test
  void aLockedSlotIsToldApartFromAnEmptyOne() {
    assertFalse(
        keyOf(OperationMessages.forDeposit(DepositOutcome.slotLocked()))
            .equals(keyOf(OperationMessages.forDeposit(DepositOutcome.nothingToDeposit()))));
  }

  @Test
  void bothSidesOfARefusalSayTheSameThing() {
    assertEquals(
        PREFIX + "refused", keyOf(OperationMessages.forWithdrawal(WithdrawOutcome.refused())));
    assertEquals(PREFIX + "refused", keyOf(OperationMessages.forDeposit(DepositOutcome.refused())));
  }

  @ParameterizedTest
  @EnumSource(WithdrawResult.class)
  void everyWithdrawalEndingIsAnsweredRatherThanFallingThrough(WithdrawResult result) {
    assertEquals(
        result != WithdrawResult.WITHDRAWN,
        OperationMessages.forWithdrawal(withdrawalEnding(result)).isPresent());
  }

  @ParameterizedTest
  @EnumSource(DepositResult.class)
  void everyDepositEndingIsAnsweredRatherThanFallingThrough(DepositResult result) {
    assertEquals(
        result != DepositResult.DEPOSITED && result != DepositResult.RATE_LIMITED,
        OperationMessages.forDeposit(depositEnding(result)).isPresent());
  }

  /**
   * A deposit that stored everything says nothing, and one refused for pace has already been
   * answered by the guard that refused it. Every other ending is spoken for here, because an ending
   * nobody explains is indistinguishable from a button that did nothing.
   */
  @Test
  void theTwoSilentEndingsAreSilentForDifferentReasons() {
    assertTrue(OperationMessages.forDeposit(DepositOutcome.stored(64L, 64L)).isEmpty());
    assertTrue(OperationMessages.forDeposit(DepositOutcome.rateLimited()).isEmpty());
    assertEquals(
        PREFIX + "cooldown.deposit_all",
        ((TranslatableTextContent)
                OperationMessages.rateLimited(ExpensiveOperation.DEPOSIT_ALL, 20L).getContent())
            .getKey());
  }

  /** Seconds rather than ticks, and never rounded down to "in zero seconds". */
  @ParameterizedTest
  @CsvSource({"1,1", "20,1", "21,2", "40,2", "41,3"})
  void theWaitIsReportedInWholeSecondsAndNeverInNone(long ticks, long seconds) {
    assertEquals(
        seconds,
        ((TranslatableTextContent)
                OperationMessages.rateLimited(ExpensiveOperation.RESCAN, ticks).getContent())
            .getArgs()[0]);
  }

  private static WithdrawOutcome withdrawalEnding(WithdrawResult result) {
    return switch (result) {
      case WITHDRAWN -> WithdrawOutcome.moved(64L, 64L);
      case PARTIAL -> WithdrawOutcome.moved(64L, 9L);
      case NOTHING_AVAILABLE -> WithdrawOutcome.nothingAvailable(64L);
      case NO_ROOM -> WithdrawOutcome.noRoom(64L);
      case REFUSED -> WithdrawOutcome.refused();
    };
  }

  private static DepositOutcome depositEnding(DepositResult result) {
    return switch (result) {
      case DEPOSITED -> DepositOutcome.stored(64L, 64L);
      case PARTIAL -> DepositOutcome.stored(64L, 20L);
      case NOTHING_TO_DEPOSIT -> DepositOutcome.nothingToDeposit();
      case SLOT_LOCKED -> DepositOutcome.slotLocked();
      case NO_SPACE -> DepositOutcome.noSpace(64L);
      case RATE_LIMITED -> DepositOutcome.rateLimited();
      case REFUSED -> DepositOutcome.refused();
    };
  }
}
