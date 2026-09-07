package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListingCriteriaTest {
  private static final ListingWindow FIRST_PAGE = ListingWindow.of(0, 45);
  private static final ListingWindow SECOND_PAGE = ListingWindow.of(45, 45);

  private final ListingCriteria criteria = new ListingCriteria();

  @Test
  void aScreenThatHasAskedForNothingWantsTheFirstThingItIsAbout() {
    assertTrue(criteria.needsRequest(FIRST_PAGE, ""));
  }

  @Test
  void theSamePageUnderTheSameQueryIsNotAskedForTwice() {
    criteria.requested(FIRST_PAGE, "chest");

    assertFalse(criteria.needsRequest(FIRST_PAGE, "chest"));
    assertFalse(criteria.isNewQuery("chest"));
  }

  @ParameterizedTest
  @CsvSource({"chest,ches", "chest,chests", "chest,''", "'',chest"})
  void aChangedQueryIsBothANewQueryAndANewRequest(String asked, String wanted) {
    criteria.requested(FIRST_PAGE, asked);

    assertTrue(criteria.isNewQuery(wanted));
    assertTrue(criteria.needsRequest(FIRST_PAGE, wanted));
  }

  @Test
  void scrollingIsANewRequestWithoutBeingANewQuery() {
    criteria.requested(FIRST_PAGE, "chest");

    assertFalse(criteria.isNewQuery("chest"));
    assertTrue(criteria.needsRequest(SECOND_PAGE, "chest"));
  }

  @Test
  void whatWasAskedForIsWhatIsSent() {
    criteria.requested(SECOND_PAGE, "iron");

    assertEquals(SECOND_PAGE, criteria.window());
    assertEquals("iron", criteria.search());
  }

  @Test
  void nothingAskedForIsAnEmptyQuestionRatherThanANullOne() {
    criteria.requested(null, null);

    assertEquals(ListingWindow.EMPTY, criteria.window());
    assertEquals("", criteria.search());
    assertFalse(criteria.isNewQuery(null));
    assertFalse(criteria.needsRequest(null, null));
  }
}
