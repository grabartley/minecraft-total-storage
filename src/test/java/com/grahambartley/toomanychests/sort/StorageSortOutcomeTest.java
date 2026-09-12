package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StorageSortOutcomeTest {
  @ParameterizedTest
  @EnumSource(
      value = StorageSortOutcome.class,
      names = {"SORTED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOutcomeExceptSortedReportsThatNothingMoved(StorageSortOutcome outcome) {
    assertFalse(outcome.wasSorted());
  }

  @ParameterizedTest
  @EnumSource(
      value = StorageSortOutcome.class,
      names = {"ALREADY_SORTED", "EMPTY", "GONE"})
  void aContainerThatNeededNothingIsNotReportedAsSkipped(StorageSortOutcome outcome) {
    assertFalse(outcome.wasSkipped());
  }

  @ParameterizedTest
  @EnumSource(
      value = StorageSortOutcome.class,
      names = {"EXCLUDED", "NOT_SORTABLE"})
  void aContainerTheOperationRefusedToTouchIsSkipped(StorageSortOutcome outcome) {
    assertTrue(outcome.wasSkipped());
  }

  @ParameterizedTest
  @EnumSource(StorageSortOutcome.class)
  void noOutcomeIsBothSortedAndSkipped(StorageSortOutcome outcome) {
    assertFalse(outcome.wasSorted() && outcome.wasSkipped());
  }

  @ParameterizedTest
  @EnumSource(StorageSortOutcome.class)
  void everyOutcomeSurvivesARoundTripThroughItsName(StorageSortOutcome outcome) {
    assertEquals(outcome, StorageSortOutcome.valueOf(outcome.name()));
  }
}
