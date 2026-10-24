package com.grahambartley.totalstorage.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ListingStatusTest {
  @DisplayName("an answered listing says which of the empty cases it is")
  @ParameterizedTest(name = "network empty {0} and anything matched {1} reads as {2}")
  @CsvSource({
    "false, true, LIVE",
    "true, false, EMPTY_NETWORK",
    "false, false, NO_RESULTS",
  })
  void namesTheSituationFromWhatTheNetworkHeldAndWhatMatched(
      boolean networkEmpty, boolean anyMatches, ListingStatus expected) {
    assertEquals(expected, ListingStatus.of(networkEmpty, anyMatches));
  }

  @Test
  void aMatchIsALiveListingEvenWhenTheIndexClaimsToBeEmpty() {
    assertEquals(ListingStatus.LIVE, ListingStatus.of(true, true));
  }

  @Test
  void onlyALiveListingHasRowsToDraw() {
    assertTrue(ListingStatus.LIVE.hasRows());
  }

  @DisplayName("every other status is a reason there are no rows rather than rows")
  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ListingStatus.class,
      names = {"PENDING", "EMPTY_NETWORK", "NO_RESULTS", "OFFLINE"})
  void everyOtherStatusHasNoRows(ListingStatus status) {
    assertFalse(status.hasRows());
  }
}
