package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LockToggleOutcomeTest {
  @ParameterizedTest
  @EnumSource(
      value = LockToggleOutcome.class,
      names = {"LOCKED", "UNLOCKED"})
  void anAcceptedToggleIsWorthPersisting(LockToggleOutcome outcome) {
    assertTrue(outcome.wasApplied());
  }

  @ParameterizedTest
  @EnumSource(
      value = LockToggleOutcome.class,
      names = {"REJECTED"})
  void aRejectedToggleIsNotWorthPersisting(LockToggleOutcome outcome) {
    assertFalse(outcome.wasApplied());
  }
}
