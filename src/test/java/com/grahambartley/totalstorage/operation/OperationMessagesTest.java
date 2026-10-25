package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  private static final String PREFIX = "operation.total-storage.";

  private static String keyOf(Optional<Text> line) {
    assertTrue(line.isPresent(), "expected a message");
    return ((TranslatableTextContent) line.get().getContent()).getKey();
  }

  private static Object[] argumentsOf(Optional<Text> line) {
    return ((TranslatableTextContent) line.get().getContent()).getArgs();
  }

  @Test
  void aWholeWithdrawalAndAWholeDepositSayNothing() {
    assertTrue(OperationMessages.forWithdrawal(WithdrawOutcome.filled(64L)).isEmpty());
    assertTrue(OperationMessages.forDeposit(DepositOutcome.stored(64L, 64L, List.of())).isEmpty());
  }

  @ParameterizedTest
  @CsvSource({"NO_STOCK, withdraw.partial.stock", "NO_ROOM, withdraw.partial.room"})
  void aPartialWithdrawalBlamesTheSideThatActuallyRanOut(ShortfallCause cause, String key) {
    Optional<Text> line = OperationMessages.forWithdrawal(WithdrawOutcome.partial(64L, 9L, cause));

    assertEquals(PREFIX + key, keyOf(line));
    assertEquals(9L, argumentsOf(line)[0]);
    assertEquals(64L, argumentsOf(line)[1]);
  }

  @Test
  void everyEmptyHandedEndingGetsAReasonOfItsOwn() {
    assertEquals(
        PREFIX + "withdraw.gone",
        keyOf(
            OperationMessages.forWithdrawal(
                WithdrawOutcome.nothingMoved(64L, ShortfallCause.NO_STOCK))));
    assertEquals(
        PREFIX + "withdraw.full",
        keyOf(
            OperationMessages.forWithdrawal(
                WithdrawOutcome.nothingMoved(64L, ShortfallCause.NO_ROOM))));
    assertEquals(
        PREFIX + "deposit.empty",
        keyOf(OperationMessages.forDeposit(DepositOutcome.nothingToDeposit())));
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
      case WITHDRAWN -> WithdrawOutcome.filled(64L);
      case PARTIAL_NO_STOCK -> WithdrawOutcome.partial(64L, 9L, ShortfallCause.NO_STOCK);
      case PARTIAL_NO_ROOM -> WithdrawOutcome.partial(64L, 9L, ShortfallCause.NO_ROOM);
      case NOTHING_AVAILABLE -> WithdrawOutcome.nothingMoved(64L, ShortfallCause.NO_STOCK);
      case NO_ROOM -> WithdrawOutcome.nothingMoved(64L, ShortfallCause.NO_ROOM);
      case REFUSED -> WithdrawOutcome.refused();
    };
  }

  private static DepositOutcome depositEnding(DepositResult result) {
    return switch (result) {
      case DEPOSITED -> DepositOutcome.stored(64L, 64L, List.of());
      case PARTIAL, NO_SPACE, ONLY_FILTERED_SPACE, NO_FITTING_SPACE -> null;
      case NOTHING_TO_DEPOSIT -> DepositOutcome.nothingToDeposit();
      case RATE_LIMITED -> DepositOutcome.rateLimited();
      case REFUSED -> DepositOutcome.refused();
    };
  }

  @ParameterizedTest
  @CsvSource({
    "WITHDRAWN, DONE",
    "PARTIAL_NO_STOCK, PARTIAL",
    "PARTIAL_NO_ROOM, PARTIAL",
    "NOTHING_AVAILABLE, FAILED",
    "NO_ROOM, FAILED",
    "REFUSED, FAILED"
  })
  void everyWithdrawalResultShowsInTheToneOfHowItWent(
      WithdrawResult result, TerminalNoticeTone tone) {
    assertEquals(tone, OperationMessages.toneOf(result));
  }

  @ParameterizedTest
  @CsvSource({
    "DEPOSITED, DONE",
    "RATE_LIMITED, DONE",
    "PARTIAL, PARTIAL",
    "NOTHING_TO_DEPOSIT, PARTIAL",
    "NO_SPACE, FAILED",
    "ONLY_FILTERED_SPACE, FAILED",
    "NO_FITTING_SPACE, FAILED",
    "REFUSED, FAILED"
  })
  void everyDepositResultShowsInTheToneOfHowItWent(DepositResult result, TerminalNoticeTone tone) {
    assertEquals(tone, OperationMessages.toneOf(result));
  }

  @ParameterizedTest
  @CsvSource({"NO_STOCK", "NO_ROOM"})
  void aPartialWithdrawalLeadsWithHowMuchItTook(ShortfallCause cause) {
    final Optional<Text> headline =
        OperationMessages.headlineForWithdrawal(WithdrawOutcome.partial(64L, 9L, cause));

    assertEquals(PREFIX + "withdraw.partial.headline", keyOf(headline));
    assertEquals(9L, argumentsOf(headline)[0]);
    assertEquals(64L, argumentsOf(headline)[1]);
  }

  @ParameterizedTest
  @CsvSource({"NO_STOCK, withdraw.gone.headline", "NO_ROOM, withdraw.full.headline"})
  void aWithdrawalThatTookNothingLeadsWithWhy(ShortfallCause cause, String key) {
    assertEquals(
        PREFIX + key,
        keyOf(OperationMessages.headlineForWithdrawal(WithdrawOutcome.nothingMoved(64L, cause))));
  }

  @Test
  void aWholeWithdrawalHasNoHeadline() {
    assertTrue(OperationMessages.headlineForWithdrawal(WithdrawOutcome.filled(64L)).isEmpty());
  }

  @Test
  void aDepositThatStoredNothingLeadsWithWhy() {
    assertEquals(
        PREFIX + "deposit.full.headline",
        keyOf(OperationMessages.headlineForDeposit(DepositOutcome.noSpace(0L, List.of()))));
    assertEquals(
        PREFIX + "deposit.filtered_only.headline",
        keyOf(
            OperationMessages.headlineForDeposit(DepositOutcome.onlyFilteredSpace(0L, List.of()))));
    assertEquals(
        PREFIX + "deposit.unfitting.headline",
        keyOf(OperationMessages.headlineForDeposit(DepositOutcome.noFittingSpace(0L, List.of()))));
  }

  @Test
  void aWholeDepositHasNoHeadline() {
    assertTrue(
        OperationMessages.headlineForDeposit(DepositOutcome.stored(64L, 64L, List.of())).isEmpty());
  }
}
