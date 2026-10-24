package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LockToggleTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 8, 9, 35})
  void togglingAFreeSlotLocksIt(int slot) {
    LockToggleResult result = LockToggle.apply(LockedSlots.empty(), slot);

    assertEquals(LockToggleOutcome.LOCKED, result.outcome());
    assertTrue(result.locks().isLocked(slot));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, 9, 35})
  void togglingALockedSlotFreesIt(int slot) {
    LockToggleResult result = LockToggle.apply(LockedSlots.empty().with(slot), slot);

    assertEquals(LockToggleOutcome.UNLOCKED, result.outcome());
    assertFalse(result.locks().isLocked(slot));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 36, 39, 40, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void aForgedIndexIsRejectedAndLeavesTheSetAlone(int slot) {
    LockedSlots existing = LockedSlots.of(List.of(2, 7));

    LockToggleResult result = LockToggle.apply(existing, slot);

    assertEquals(LockToggleOutcome.REJECTED, result.outcome());
    assertSame(existing, result.locks());
  }

  @Test
  void togglingOneSlotLeavesTheOthersLocked() {
    LockedSlots existing = LockedSlots.of(List.of(2, 7, 20));

    LockToggleResult result = LockToggle.apply(existing, 7);

    assertEquals(java.util.Set.of(2, 20), result.locks().indices());
  }

  @Test
  void aPlayerWithNoStoredSetStillLocks() {
    LockToggleResult result = LockToggle.apply(null, 4);

    assertEquals(LockToggleOutcome.LOCKED, result.outcome());
    assertTrue(result.locks().isLocked(4));
  }

  @Test
  void aForgedIndexAgainstNoStoredSetReportsAnEmptySetRatherThanNull() {
    LockToggleResult result = LockToggle.apply(null, 99);

    assertEquals(LockToggleOutcome.REJECTED, result.outcome());
    assertTrue(result.locks().isEmpty());
  }

  @Test
  void twoTogglesOfTheSameSlotReturnItToWhereItStarted() {
    LockedSlots start = LockedSlots.of(List.of(11));

    LockedSlots once = LockToggle.apply(start, 11).locks();
    LockedSlots twice = LockToggle.apply(once, 11).locks();

    assertEquals(start, twice);
  }
}
