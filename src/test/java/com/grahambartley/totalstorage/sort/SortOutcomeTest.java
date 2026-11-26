package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class SortOutcomeTest {
  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {"SORTED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOutcomeExceptSortedReportsThatNothingWasSorted(SortOutcome outcome) {
    assertFalse(outcome.wasSorted());
  }

  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {
        "NOT_OFFERED",
        "NO_OPEN_SCREEN",
        "SCREEN_NOT_SORTABLE",
        "NOT_OPENABLE",
        "RATE_LIMITED"
      })
  void anOutcomeWhereTheModDeclinedToActIsARefusal(SortOutcome outcome) {
    assertTrue(outcome.wasRefused());
  }

  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {"NOTHING_TO_SORT", "ALREADY_SORTED", "FAILED", "SORTED"})
  void anOutcomeWhereTheModActuallyLookedAtTheContainerIsNotARefusal(SortOutcome outcome) {
    assertFalse(outcome.wasRefused());
  }

  @ParameterizedTest
  @EnumSource(SortOutcome.class)
  void noOutcomeIsBothSortedAndRefused(SortOutcome outcome) {
    assertFalse(outcome.wasSorted() && outcome.wasRefused());
  }

  @ParameterizedTest
  @EnumSource(SortOutcome.class)
  void everyOutcomeSurvivesARoundTripThroughItsName(SortOutcome outcome) {
    assertEquals(outcome, SortOutcome.valueOf(outcome.name()));
  }

  @ParameterizedTest
  @CsvSource({
    "SORTED, DONE",
    "ALREADY_SORTED, DONE",
    "NOTHING_TO_SORT, DONE",
    "RATE_LIMITED, DONE",
    "NOT_OFFERED, FAILED",
    "NO_OPEN_SCREEN, FAILED",
    "SCREEN_NOT_SORTABLE, FAILED",
    "NOT_OPENABLE, FAILED",
    "FAILED, FAILED",
  })
  void everyOutcomeCarriesTheToneItIsReportedIn(SortOutcome outcome, TerminalNoticeTone tone) {
    assertEquals(tone, outcome.tone());
  }
}
