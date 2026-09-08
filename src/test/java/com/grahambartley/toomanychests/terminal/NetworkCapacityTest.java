package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * How full a network reads as being.
 *
 * <p>The number is the one thing standing between a player and finding out they are full on a
 * failed deposit, so the cases that matter are the edges: a network with a single slot left must
 * not read as full, and one nobody can write to must not read as either.
 */
class NetworkCapacityTest {

  @Test
  void whatIsFreeIsWhatIsNotUsed() {
    NetworkCapacity capacity = new NetworkCapacity(20, 54);

    assertEquals(34, capacity.freeSlots());
    assertEquals(20, capacity.usedSlots());
    assertEquals(54, capacity.totalSlots());
  }

  /** Rounded up, so the one case the number warns about is never announced early. */
  @ParameterizedTest
  @CsvSource({"0,54,0", "1,54,2", "27,54,50", "53,54,99", "54,54,100", "1,1000,1"})
  void howFullItReadsRoundsUpSoAFreeSlotNeverReadsAsFull(int used, int total, int percent) {
    assertEquals(percent, new NetworkCapacity(used, total).percentUsed());
  }

  @Test
  void aSingleFreeSlotIsNotFull() {
    NetworkCapacity capacity = new NetworkCapacity(53, 54);

    assertFalse(capacity.isFull());
    assertEquals(1, capacity.freeSlots());
  }

  @Test
  void aNetworkWithNoFreeSlotIsFull() {
    assertTrue(new NetworkCapacity(54, 54).isFull());
  }

  /** No containers is not the same as no room, and a player can do something about only one. */
  @Test
  void aNetworkWithNoWritableContainersIsUnknownRatherThanFull() {
    NetworkCapacity nothing = NetworkCapacity.EMPTY;

    assertTrue(nothing.isUnknown());
    assertFalse(nothing.isFull());
    assertEquals(0, nothing.percentUsed());
    assertFalse(nothing.isNearCapacity(90));
  }

  @Test
  void theWarningStartsAtTheThresholdRatherThanPastIt() {
    assertTrue(new NetworkCapacity(90, 100).isNearCapacity(90));
    assertFalse(new NetworkCapacity(89, 100).isNearCapacity(90));
  }

  /** A threshold of nothing is a warning switched off, not one that fires on an empty network. */
  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void aThresholdOfNothingNeverWarns(int threshold) {
    assertFalse(new NetworkCapacity(100, 100).isNearCapacity(threshold));
  }

  @Test
  void aThresholdOfAHundredWarnsOnlyWhenReallyFull() {
    assertTrue(new NetworkCapacity(54, 54).isNearCapacity(100));
    assertFalse(new NetworkCapacity(53, 54).isNearCapacity(100));
  }

  @Test
  void usingMoreSlotsThanExistIsRefusedRatherThanDrawn() {
    assertThrows(IllegalArgumentException.class, () -> new NetworkCapacity(10, 5));
  }

  @ParameterizedTest
  @CsvSource({"-1,5", "5,-1"})
  void countingBelowZeroIsRefused(int used, int total) {
    assertThrows(IllegalArgumentException.class, () -> new NetworkCapacity(used, total));
  }
}
