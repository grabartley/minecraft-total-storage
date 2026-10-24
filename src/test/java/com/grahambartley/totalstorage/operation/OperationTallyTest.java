package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.sort.StorageSortOutcome;
import com.grahambartley.totalstorage.sort.StorageSortResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OperationTallyTest {
  @Test
  void aTallyStartsAtNoStepsTaken() {
    OperationTally tally = new OperationTally(3);

    assertEquals(0, tally.report().stepsDone());
    assertEquals(3, tally.report().stepsTotal());
  }

  @Test
  void aTallyCannotPlanANegativeNumberOfSteps() {
    assertThrows(IllegalArgumentException.class, () -> new OperationTally(-1));
  }

  @Test
  void stepsNeverRunPastTheTotalPlanned() {
    OperationTally tally = new OperationTally(1);

    tally.stepped();
    tally.stepped();

    assertEquals(1, tally.report().stepsDone());
  }

  @Test
  void finishingCountsEveryStepThePlanAllowedFor() {
    OperationTally tally = new OperationTally(4);
    tally.stepped();

    tally.finished();

    assertEquals(4, tally.report().stepsDone());
    assertEquals(100, tally.report().percentDone());
  }

  @Test
  void aSortedContainerCountsItsMergedStacks() {
    OperationTally tally = new OperationTally(1);

    tally.sorted(new StorageSortResult(StorageSortOutcome.SORTED, 5, 2));

    assertEquals(1, tally.report().containersSorted());
    assertEquals(3, tally.report().stacksMerged());
  }

  @ParameterizedTest
  @EnumSource(
      value = StorageSortOutcome.class,
      names = {"ALREADY_SORTED", "EMPTY", "FAILED"})
  void aContainerThatNeededNothingIsNotCountedAsSorted(StorageSortOutcome outcome) {
    OperationTally tally = new OperationTally(1);

    tally.sorted(StorageSortResult.of(outcome));

    assertEquals(0, tally.report().containersSorted());
    assertFalse(tally.report().skippedAnything());
  }

  @Test
  void anExcludedContainerIsReportedAsSkipped() {
    OperationTally tally = new OperationTally(1);

    tally.sorted(StorageSortResult.of(StorageSortOutcome.EXCLUDED));

    assertEquals(1, tally.report().excludedSkipped());
    assertEquals(0, tally.report().restrictedSkipped());
  }

  @Test
  void aContainerWithSlotsTheModWillNotRearrangeIsReportedSeparately() {
    OperationTally tally = new OperationTally(1);

    tally.sorted(StorageSortResult.of(StorageSortOutcome.NOT_SORTABLE));

    assertEquals(0, tally.report().excludedSkipped());
    assertEquals(1, tally.report().restrictedSkipped());
  }

  @Test
  void onlyPositiveMovesAreCounted() {
    OperationTally tally = new OperationTally(1);

    tally.moved(12L);
    tally.moved(0L);
    tally.moved(-5L);

    assertEquals(12L, tally.report().itemsMoved());
  }

  @Test
  void cancellingIsCarriedIntoTheReport() {
    OperationTally tally = new OperationTally(2);

    tally.cancelled();

    assertTrue(tally.wasCancelled());
    assertTrue(tally.report().cancelled());
  }
}
