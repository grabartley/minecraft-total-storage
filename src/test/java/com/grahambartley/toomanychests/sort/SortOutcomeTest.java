package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
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
      names = {"NO_OPEN_SCREEN", "SCREEN_NOT_SORTABLE"})
  void anOutcomeDecidedBeforeTheItemsWereReachedIsARefusal(SortOutcome outcome) {
    assertTrue(outcome.wasRefused());
  }

  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {"NOTHING_TO_SORT", "ALREADY_SORTED", "FAILED", "SORTED"})
  void anOutcomeReachedAfterInspectingTheContainerIsNotARefusal(SortOutcome outcome) {
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
}
