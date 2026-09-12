package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class QuickStackResultTest {
  @ParameterizedTest
  @EnumSource(
      value = QuickStackResult.class,
      names = {"STACKED"})
  void onlyAStackedResultMovedAnything(QuickStackResult result) {
    assertTrue(result.movedAnything());
  }

  @ParameterizedTest
  @EnumSource(
      value = QuickStackResult.class,
      names = {"STACKED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOtherResultLeftTheInventoryAlone(QuickStackResult result) {
    assertFalse(result.movedAnything());
  }

  @ParameterizedTest
  @EnumSource(QuickStackResult.class)
  void everyResultSurvivesARoundTripThroughItsName(QuickStackResult result) {
    assertEquals(result, QuickStackResult.valueOf(result.name()));
  }
}
