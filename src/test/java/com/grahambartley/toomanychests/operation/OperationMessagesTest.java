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

class OperationMessagesTest {
  private static final String PREFIX = "operation.too-many-chests.";

  private static String keyOf(Optional<Text> line) {
    assertTrue(line.isPresent(), "expected a message");
    return ((TranslatableTextContent) line.get().getContent()).getKey();
  }

  private static Object[] argumentsOf(Optional<Text> line) {
    return ((TranslatableTextContent) line.get().getContent()).getArgs();
  }

  @Test
  void aWholeWithdrawalAndAWholeDepositSayNothing() {
    assertTrue(OperationMessages.forWithdrawal(WithdrawOutcome.moved(64L, 64L)).isEmpty());
    assertTrue(OperationMessages.forDeposit(DepositOutcome.stored(64L, 64L, List.of())).isEmpty());
  }

  @Test
  void aPartialWithdrawalNamesBothWhatArrivedAndWhatWasAskedFor() {
    Optional<Text> line = OperationMessages.forWithdrawal(WithdrawOutcome.moved(64L, 9L));

    assertEquals(PREFIX + "withdraw.partial", keyOf(line));
    assertEquals(9L, argumentsOf(line)[0]);
    assertEquals(64L, argumentsOf(line)[1]);
  }

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
        PREFIX + "deposit.locked",
        keyOf(OperationMessages.forDeposit(DepositOutcome.slotLocked())));
  }

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
  void everyDepositEndingThatCanBeBuiltHereIsAnsweredRatherThanFallingThrough(
      DepositResult result) {
    DepositOutcome ending = depositEnding(result);
    if (ending == null) {
      return;
    }
    assertEquals(
        result != DepositResult.DEPOSITED && result != DepositResult.RATE_LIMITED,
        OperationMessages.forDeposit(ending).isPresent());
  }

  @Test
  void theTwoSilentEndingsAreSilentForDifferentReasons() {
    assertTrue(OperationMessages.forDeposit(DepositOutcome.stored(64L, 64L, List.of())).isEmpty());
    assertTrue(OperationMessages.forDeposit(DepositOutcome.rateLimited()).isEmpty());
    assertEquals(
        PREFIX + "cooldown.deposit_all",
        ((TranslatableTextContent)
                OperationMessages.rateLimited(ExpensiveOperation.DEPOSIT_ALL, 20L).getContent())
            .getKey());
  }

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
      case DEPOSITED -> DepositOutcome.stored(64L, 64L, List.of());
      case PARTIAL, NO_SPACE -> null;
      case NOTHING_TO_DEPOSIT -> DepositOutcome.nothingToDeposit();
      case SLOT_LOCKED -> DepositOutcome.slotLocked();
      case RATE_LIMITED -> DepositOutcome.rateLimited();
      case REFUSED -> DepositOutcome.refused();
    };
  }
}
