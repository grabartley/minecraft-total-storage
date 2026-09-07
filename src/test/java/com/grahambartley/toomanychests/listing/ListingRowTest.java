package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ListingRowTest {
  @Test
  void aRowCarriesWhatItIsAndHowMuchTheNetworkHolds() {
    ListingRow<String> row = new ListingRow<>("cobblestone", 300L);

    assertEquals("cobblestone", row.variant());
    assertEquals(300L, row.count());
  }

  @Test
  void aRowHoldingNoneOfSomethingIsStillARow() {
    assertEquals(0L, new ListingRow<>("cobblestone", 0L).count());
  }

  @Test
  void aRowRefusesToNameNothing() {
    assertThrows(NullPointerException.class, () -> new ListingRow<>(null, 1L));
  }

  @DisplayName("a count that could never have been counted is refused rather than drawn")
  @ParameterizedTest(name = "count {0}")
  @ValueSource(longs = {-1L, -300L, Long.MIN_VALUE})
  void aRowRefusesACountThatCannotExist(long count) {
    assertThrows(IllegalArgumentException.class, () -> new ListingRow<>("cobblestone", count));
  }
}
