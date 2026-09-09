package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ListingPlaceholderTest {
  @DisplayName("a grid with rows to draw shows them rather than a message")
  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ListingStatus.class,
      names = {"LIVE", "PENDING"})
  void aLiveOrUnansweredListingShowsNoPlaceholder(ListingStatus status) {
    assertEquals(Optional.empty(), ListingPlaceholder.forStatus(status));
  }

  @DisplayName("every reason a grid has no rows gets said out loud")
  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ListingStatus.class,
      names = {"EMPTY_NETWORK", "NO_RESULTS", "OFFLINE"})
  void everyEmptyCaseGetsSomethingToShow(ListingStatus status) {
    assertTrue(ListingPlaceholder.forStatus(status).isPresent());
  }

  @Test
  void anEmptyNetworkAndAnEmptySearchAreDifferentPictures() {
    assertNotEquals(
        ListingPlaceholder.forStatus(ListingStatus.EMPTY_NETWORK).orElseThrow().sprite(),
        ListingPlaceholder.forStatus(ListingStatus.NO_RESULTS).orElseThrow().sprite());
  }

  @Test
  void aTornDownNetworkReadsDifferentlyFromAnEmptyOne() {
    assertNotEquals(
        ListingPlaceholder.forStatus(ListingStatus.EMPTY_NETWORK).orElseThrow().message(),
        ListingPlaceholder.forStatus(ListingStatus.OFFLINE).orElseThrow().message());
  }
}
