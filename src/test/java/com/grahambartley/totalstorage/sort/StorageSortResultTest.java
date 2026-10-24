package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class StorageSortResultTest {
  @ParameterizedTest
  @CsvSource({"6, 2, 4", "3, 3, 0", "0, 0, 0"})
  void mergedStacksAreTheDifferenceBetweenBeforeAndAfter(int before, int after, int merged) {
    assertEquals(
        merged, new StorageSortResult(StorageSortOutcome.SORTED, before, after).stacksMerged());
  }

  @Test
  void aContainerThatSplitAStackIntoMoreSlotsMergedNothing() {
    assertEquals(0, new StorageSortResult(StorageSortOutcome.SORTED, 2, 3).stacksMerged());
  }

  @ParameterizedTest
  @CsvSource({"0, 0, 1", "3, 1, 3", "1, 4, 4"})
  void theSlotsReadAreTheMostTheContainerHeldEitherSide(int before, int after, int read) {
    assertEquals(read, new StorageSortResult(StorageSortOutcome.SORTED, before, after).slotsRead());
  }

  @Test
  void aSortCannotCountNegativeStacks() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new StorageSortResult(StorageSortOutcome.SORTED, -1, -1));
  }

  @Test
  void aResultNeedsAnOutcome() {
    assertThrows(NullPointerException.class, () -> new StorageSortResult(null, 0, 0));
  }

  @ParameterizedTest
  @EnumSource(StorageSortOutcome.class)
  void anOutcomeOnItsOwnCountsNoStacks(StorageSortOutcome outcome) {
    StorageSortResult result = StorageSortResult.of(outcome);

    assertEquals(outcome, result.outcome());
    assertEquals(0, result.stacksMerged());
  }
}
