package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OperationReportTest {
  private static OperationReport report(int stepsDone, int stepsTotal) {
    return new OperationReport(stepsDone, stepsTotal, 0, 0, 0L, 0, 0, false);
  }

  @Test
  void aReportOfNothingHasTakenNoStepsAndSkippedNothing() {
    OperationReport report = OperationReport.nothing();

    assertEquals(0, report.stepsDone());
    assertFalse(report.skippedAnything());
    assertFalse(report.cancelled());
  }

  @ParameterizedTest
  @CsvSource({"0, 4, 0", "1, 4, 25", "3, 4, 75", "4, 4, 100", "1, 3, 33", "0, 0, 100"})
  void progressIsTheShareOfStepsTaken(int stepsDone, int stepsTotal, int percent) {
    assertEquals(percent, report(stepsDone, stepsTotal).percentDone());
  }

  @Test
  void anOperationCannotTakeMoreStepsThanItPlanned() {
    assertThrows(IllegalArgumentException.class, () -> report(5, 4));
  }

  @Test
  void anOperationCannotTakeNegativeSteps() {
    assertThrows(IllegalArgumentException.class, () -> report(-1, 0));
  }

  @Test
  void anOperationCannotCountNegativeContainers() {
    assertThrows(
        IllegalArgumentException.class, () -> new OperationReport(0, 0, -1, 0, 0L, 0, 0, false));
  }

  @Test
  void anOperationCannotMoveANegativeNumberOfItems() {
    assertThrows(
        IllegalArgumentException.class, () -> new OperationReport(0, 0, 0, 0, -1L, 0, 0, false));
  }

  @ParameterizedTest
  @CsvSource({"1, 0", "0, 1", "2, 3"})
  void anythingSkippedIsWorthReporting(int excluded, int restricted) {
    assertTrue(new OperationReport(1, 1, 0, 0, 0L, excluded, restricted, false).skippedAnything());
  }
}
