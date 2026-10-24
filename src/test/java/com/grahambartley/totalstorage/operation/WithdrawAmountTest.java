package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.lock.LockedSlots;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WithdrawAmountTest {
  @ParameterizedTest(name = "{0} held, stacking to {1}, a stack gesture asks for {2}")
  @CsvSource({
    "1, 64, 1",
    "48, 64, 48",
    "64, 64, 64",
    "12300, 64, 64",
    "1, 16, 1",
    "12, 16, 12",
    "16, 16, 16",
    "12300, 16, 16",
    "1, 1, 1",
    "48, 1, 1",
    "12300, 1, 1"
  })
  void aStackGestureAsksForWhatTheCellHoldsUpToOneStack(long held, int maxStackSize, int expected) {
    assertEquals(expected, WithdrawAmount.stackFrom(maxStackSize, held));
  }

  @ParameterizedTest(name = "{0} held, stacking to {1}, half a stack is {2}")
  @CsvSource({
    "1, 64, 1",
    "3, 64, 2",
    "48, 64, 24",
    "63, 64, 32",
    "64, 64, 32",
    "12300, 64, 32",
    "1, 16, 1",
    "9, 16, 5",
    "12300, 16, 8",
    "1, 1, 1",
    "12300, 1, 1",
    "12300, 99, 50"
  })
  void halfAStackIsHalfOfWhatTheCellHoldsRoundedUp(long held, int maxStackSize, int expected) {
    assertEquals(expected, WithdrawAmount.halfStackFrom(maxStackSize, held));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -64})
  void anItemThatStacksToNothingYieldsNothing(int maxStackSize) {
    assertEquals(0, WithdrawAmount.stackFrom(maxStackSize, 64L));
    assertEquals(0, WithdrawAmount.halfStackFrom(maxStackSize, 64L));
    assertEquals(0L, WithdrawAmount.ceilingFor(maxStackSize, WithdrawDestination.CURSOR));
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
  void aCellHoldingNothingYieldsNothing(long held) {
    assertEquals(0, WithdrawAmount.stackFrom(64, held));
    assertEquals(0, WithdrawAmount.halfStackFrom(64, held));
  }

  @Test
  void aCellHoldingMoreThanAnIntCanCountStillYieldsOneStack() {
    assertEquals(64, WithdrawAmount.stackFrom(64, Long.MAX_VALUE));
    assertEquals(32, WithdrawAmount.halfStackFrom(64, Long.MAX_VALUE));
  }

  @Test
  void theCursorHoldsOneStackHoweverMuchIsAskedFor() {
    assertEquals(64L, WithdrawAmount.resolve(4096L, 64, WithdrawDestination.CURSOR));
  }

  @Test
  void theInventoryHoldsAStackPerSlotItCouldUse() {
    assertEquals(
        (long) 64 * LockedSlots.SLOT_COUNT,
        WithdrawAmount.resolve(Long.MAX_VALUE, 64, WithdrawDestination.INVENTORY));
  }

  @ParameterizedTest
  @ValueSource(longs = {Long.MAX_VALUE, 1_000_000L, 4096L})
  void anAbsurdRequestIsClampedRatherThanRefused(long requested) {
    assertEquals(64L, WithdrawAmount.resolve(requested, 64, WithdrawDestination.CURSOR));
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
  void askingForNothingOrLessTakesNothing(long requested) {
    assertEquals(0L, WithdrawAmount.resolve(requested, 64, WithdrawDestination.CURSOR));
    assertEquals(0L, WithdrawAmount.resolve(requested, 64, WithdrawDestination.INVENTORY));
  }

  @Test
  void anAmountThatAlreadyFitsIsLeftAlone() {
    assertEquals(9L, WithdrawAmount.resolve(9L, 64, WithdrawDestination.CURSOR));
  }

  @Test
  void anUnstackableItemIsCappedAtOnePerSlot() {
    assertEquals(1L, WithdrawAmount.resolve(64L, 1, WithdrawDestination.CURSOR));
    assertEquals(
        (long) LockedSlots.SLOT_COUNT,
        WithdrawAmount.resolve(64L, 1, WithdrawDestination.INVENTORY));
  }
}
