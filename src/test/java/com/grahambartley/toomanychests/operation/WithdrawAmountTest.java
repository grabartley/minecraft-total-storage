package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.lock.LockedSlots;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WithdrawAmountTest {

  @ParameterizedTest
  @CsvSource({"64, 32", "16, 8", "1, 1", "3, 2", "99, 50"})
  void halfAStackRoundsUpAsVanillaSplitsOne(int maxStackSize, int expected) {
    assertEquals(expected, WithdrawAmount.halfStack(maxStackSize));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -64})
  void anItemThatStacksToNothingYieldsNothing(int maxStackSize) {
    assertEquals(0, WithdrawAmount.fullStack(maxStackSize));
    assertEquals(0, WithdrawAmount.halfStack(maxStackSize));
    assertEquals(0L, WithdrawAmount.ceilingFor(maxStackSize, WithdrawDestination.CURSOR));
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

  /** A forged request is bounded before anything is read, not after items have moved. */
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
