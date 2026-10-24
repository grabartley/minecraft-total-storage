package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkTagEntryTest {
  @ParameterizedTest
  @CsvSource({"Castle,castle", "  WOOD  ,wood"})
  void theIdIsNormalisedWhileTheNameIsLeftAsWritten(String id, String expected) {
    final NetworkTagEntry entry = new NetworkTagEntry(id, "Castle Build", false, 0, 0);
    assertEquals(expected, entry.id());
    assertEquals("Castle Build", entry.displayName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void anEntryWithoutAnIdIsRefused(String id) {
    assertThrows(
        IllegalArgumentException.class, () -> new NetworkTagEntry(id, "Castle", false, 0, 0));
  }

  @Test
  void anEntryWithoutANameFallsBackToItsId() {
    assertEquals("castle", new NetworkTagEntry("castle", null, false, 0, 0).displayName());
    assertEquals("castle", new NetworkTagEntry("castle", "  ", false, 0, 0).displayName());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -50})
  void negativeCountsAreClampedRatherThanShown(int count) {
    final NetworkTagEntry entry = new NetworkTagEntry("castle", "Castle", false, count, count);
    assertEquals(0, entry.assignedItems());
    assertEquals(0, entry.matchingItems());
  }

  @Test
  void aTagMatchingNothingHereSaysSoWithoutBeingEmptyEverywhere() {
    assertTrue(new NetworkTagEntry("castle", "Castle", false, 12, 0).isEmptyHere());
    assertFalse(new NetworkTagEntry("castle", "Castle", false, 12, 1).isEmptyHere());
  }
}
