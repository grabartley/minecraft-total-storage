package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListingRowTest {
  @Test
  void aRowCarriesWhatItIsHowMuchTheNetworkHoldsAndAcrossHowManyContainers() {
    ListingRow<String> row = new ListingRow<>("cobblestone", 300L, 4);

    assertEquals("cobblestone", row.variant());
    assertEquals(300L, row.count());
    assertEquals(4, row.providerCount());
  }

  @Test
  void aRowHoldingNoneOfSomethingIsStillARow() {
    assertEquals(0L, new ListingRow<>("cobblestone", 0L, 0).count());
  }

  @Test
  void aRowRefusesToNameNothing() {
    assertThrows(NullPointerException.class, () -> new ListingRow<>(null, 1L, 1));
  }

  @DisplayName("a count that could never have been counted is refused rather than drawn")
  @ParameterizedTest(name = "count {0}")
  @ValueSource(longs = {-1L, -300L, Long.MIN_VALUE})
  void aRowRefusesACountThatCannotExist(long count) {
    assertThrows(IllegalArgumentException.class, () -> new ListingRow<>("cobblestone", count, 1));
  }

  @DisplayName("a container count that could never have been counted is refused")
  @ParameterizedTest(name = "providerCount {0}")
  @ValueSource(ints = {-1, -300, Integer.MIN_VALUE})
  void aRowRefusesAProviderCountThatCannotExist(int providerCount) {
    assertThrows(
        IllegalArgumentException.class, () -> new ListingRow<>("cobblestone", 1L, providerCount));
  }

  @DisplayName("only a spread across more than one container is worth saying out loud")
  @ParameterizedTest(name = "{0} containers is worth saying: {1}")
  @CsvSource({"0, false", "1, false", "2, true", "12, true"})
  void aRowKnowsWhetherItsContainerCountTellsThePlayerAnything(
      int providerCount, boolean worthSaying) {
    ListingRow<String> row = new ListingRow<>("cobblestone", 300L, providerCount);

    if (worthSaying) {
      assertTrue(row.isSpreadAcrossContainers());
    } else {
      assertFalse(row.isSpreadAcrossContainers());
    }
  }
}
