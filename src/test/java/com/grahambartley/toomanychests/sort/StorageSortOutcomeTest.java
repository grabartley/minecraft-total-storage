package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
  @EnumSource(StorageSortOutcome.class)
  void everyOutcomeSurvivesARoundTripThroughItsName(StorageSortOutcome outcome) {
    assertEquals(outcome, StorageSortOutcome.valueOf(outcome.name()));
  }
}
