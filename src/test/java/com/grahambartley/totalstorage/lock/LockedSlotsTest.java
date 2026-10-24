package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LockedSlotsTest {
  @Test
  void aFreshSetLocksNothing() {
    LockedSlots locks = LockedSlots.empty();

    assertTrue(locks.isEmpty());
    assertEquals(0, locks.count());
    assertFalse(locks.isLocked(0));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, 9, 35})
  void everyMainInventoryAndHotbarSlotCanBeLocked(int slot) {
    assertTrue(LockedSlots.isValidSlot(slot));
    assertTrue(LockedSlots.empty().with(slot).isLocked(slot));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 36, 40, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void slotsOutsideTheMainInventoryAreRejectedRatherThanStored(int slot) {
    assertFalse(LockedSlots.isValidSlot(slot));
    assertTrue(LockedSlots.empty().with(slot).isEmpty());
    assertTrue(LockedSlots.of(List.of(slot)).isEmpty());
  }

  @Test
  void outOfRangeIndicesAreDroppedWithoutLosingTheValidOnesBesideThem() {
    LockedSlots locks = LockedSlots.of(List.of(-5, 3, 36, 20, 999));

    assertEquals(Set.of(3, 20), locks.indices());
  }

  @Test
  void aNullIndexInStoredDataIsDroppedRatherThanThrowing() {
    LockedSlots locks = LockedSlots.of(Arrays.asList(4, null, 7));

    assertEquals(Set.of(4, 7), locks.indices());
  }

  @Test
  void lockingAnAlreadyLockedSlotChangesNothing() {
    LockedSlots locked = LockedSlots.empty().with(5);

    assertSame(locked, locked.with(5));
  }

  @Test
  void unlockingASlotThatWasNeverLockedChangesNothing() {
    LockedSlots locks = LockedSlots.empty().with(5);

    assertSame(locks, locks.without(6));
  }

  @Test
  void unlockingLeavesTheOtherLocksInPlace() {
    LockedSlots locks = LockedSlots.of(List.of(1, 2, 3)).without(2);

    assertEquals(Set.of(1, 3), locks.indices());
  }

  @Test
  void togglingASlotTwiceReturnsToTheOriginalSet() {
    LockedSlots original = LockedSlots.of(List.of(1, 9));

    assertEquals(original, original.toggled(4).toggled(4));
  }

  @Test
  void togglingAnOutOfRangeSlotLeavesTheSetAlone() {
    LockedSlots original = LockedSlots.of(List.of(1, 9));

    assertEquals(original, original.toggled(36));
  }

  @Test
  void mutatingAnExistingSetLeavesTheOriginalUntouched() {
    LockedSlots original = LockedSlots.of(List.of(1));
    LockedSlots extended = original.with(2);

    assertEquals(Set.of(1), original.indices());
    assertEquals(Set.of(1, 2), extended.indices());
  }

  @Test
  void indicesReadBackInAscendingOrderWhateverOrderTheyArrivedIn() {
    LockedSlots locks = LockedSlots.of(List.of(30, 2, 17, 0));

    assertEquals(List.of(0, 2, 17, 30), locks.sortedIndices());
  }

  @Test
  void twoSetsHoldingTheSameSlotsAreEqualWhateverOrderBuiltThemUp() {
    LockedSlots ascending = LockedSlots.empty().with(1).with(4).with(9);
    LockedSlots descending = LockedSlots.empty().with(9).with(4).with(1);

    assertEquals(ascending, descending);
    assertEquals(ascending.hashCode(), descending.hashCode());
  }

  @Test
  void repeatedIndicesCollapseIntoOneLock() {
    LockedSlots locks = LockedSlots.of(List.of(7, 7, 7));

    assertEquals(1, locks.count());
  }
}
