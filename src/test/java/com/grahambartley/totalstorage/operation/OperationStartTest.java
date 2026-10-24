package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OperationStartTest {
  @ParameterizedTest
  @EnumSource(
      value = OperationStart.class,
      names = {"STARTED", "CANCELLED"})
  void startingAndStoppingBothCountAsSomethingHappening(OperationStart start) {
    assertTrue(start.happened());
  }

  @ParameterizedTest
  @EnumSource(
      value = OperationStart.class,
      names = {"STARTED", "CANCELLED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOtherAnswerLeavesTheNetworkUntouched(OperationStart start) {
    assertFalse(start.happened());
  }

  @ParameterizedTest
  @EnumSource(OperationStart.class)
  void everyAnswerSurvivesARoundTripThroughItsName(OperationStart start) {
    assertEquals(start, OperationStart.valueOf(start.name()));
  }
}
