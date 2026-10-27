package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListingCriteriaTest {
  private static final ListingWindow FIRST_PAGE = ListingWindow.of(0, 45);
  private static final ListingWindow SECOND_PAGE = ListingWindow.of(45, 45);

  private final ListingCriteria criteria = new ListingCriteria();

  @Test
  void aScreenThatHasAskedForNothingWantsTheFirstThingItIsAbout() {
    assertTrue(criteria.needsRequest(FIRST_PAGE, "", Set.of(), TagCombinationMode.DEFAULT));
  }

  @Test
  void theSamePageUnderTheSameQueryIsNotAskedForTwice() {
    criteria.requested(FIRST_PAGE, "chest", Set.of(), TagCombinationMode.DEFAULT);

    assertFalse(criteria.needsRequest(FIRST_PAGE, "chest", Set.of(), TagCombinationMode.DEFAULT));
    assertFalse(criteria.isNewQuery("chest"));
  }

  @ParameterizedTest
  @CsvSource({"chest,ches", "chest,chests", "chest,''", "'',chest"})
  void aChangedQueryIsBothANewQueryAndANewRequest(String asked, String wanted) {
    criteria.requested(FIRST_PAGE, asked, Set.of(), TagCombinationMode.DEFAULT);

    assertTrue(criteria.isNewQuery(wanted));
    assertTrue(criteria.needsRequest(FIRST_PAGE, wanted, Set.of(), TagCombinationMode.DEFAULT));
  }

  @Test
  void scrollingIsANewRequestWithoutBeingANewQuery() {
    criteria.requested(FIRST_PAGE, "chest", Set.of(), TagCombinationMode.DEFAULT);

    assertFalse(criteria.isNewQuery("chest"));
    assertTrue(criteria.needsRequest(SECOND_PAGE, "chest", Set.of(), TagCombinationMode.DEFAULT));
  }

  @ParameterizedTest(name = "asked for {0}+{1}, needs {2}+{3}: new request {4}")
  @CsvSource({
    "0, 81, 0, 63, false",
    "0, 81, 9, 72, false",
    "0, 81, 18, 72, true",
    "54, 117, 45, 81, true",
    "54, 117, 72, 81, false"
  })
  void onlyRowsOutsideWhatWasAskedForAreANewRequest(
      int askedOffset, int askedLength, int neededOffset, int neededLength, boolean expected) {
    criteria.requested(
        ListingWindow.of(askedOffset, askedLength), "", Set.of(), TagCombinationMode.DEFAULT);

    assertEquals(
        expected,
        criteria.needsRequest(
            ListingWindow.of(neededOffset, neededLength),
            "",
            Set.of(),
            TagCombinationMode.DEFAULT));
  }

  @Test
  void whatWasAskedForIsWhatIsSent() {
    criteria.requested(SECOND_PAGE, "iron", Set.of(), TagCombinationMode.DEFAULT);

    assertEquals(SECOND_PAGE, criteria.window());
    assertEquals("iron", criteria.search());
  }

  @Test
  void turningATagOnIsANewQuestionEvenOnTheSamePage() {
    criteria.requested(FIRST_PAGE, "chest", Set.of(), TagCombinationMode.DEFAULT);

    assertTrue(criteria.isNewFilter(Set.of("wood"), TagCombinationMode.DEFAULT));
    assertTrue(
        criteria.needsRequest(FIRST_PAGE, "chest", Set.of("wood"), TagCombinationMode.DEFAULT));
  }

  @Test
  void changingHowTagsCombineIsANewQuestion() {
    criteria.requested(FIRST_PAGE, "", Set.of("wood"), TagCombinationMode.AND);

    assertTrue(criteria.isNewFilter(Set.of("wood"), TagCombinationMode.OR));
    assertTrue(criteria.needsRequest(FIRST_PAGE, "", Set.of("wood"), TagCombinationMode.OR));
  }

  @Test
  void theSameTagsInAnotherOrderOrCaseAreNotANewQuestion() {
    criteria.requested(FIRST_PAGE, "", Set.of("wood", "stone"), TagCombinationMode.AND);

    assertFalse(criteria.isNewFilter(Set.of("STONE", "Wood"), TagCombinationMode.AND));
    assertFalse(
        criteria.needsRequest(FIRST_PAGE, "", Set.of("stone", "wood"), TagCombinationMode.AND));
  }

  @Test
  void whatWasFilteredOnIsWhatIsSent() {
    criteria.requested(FIRST_PAGE, "", Set.of("Wood"), TagCombinationMode.OR);

    assertEquals(Set.of("wood"), criteria.tags());
    assertEquals(TagCombinationMode.OR, criteria.mode());
  }

  @Test
  void noFilterAtAllDefaultsToNarrowingRatherThanNull() {
    criteria.requested(FIRST_PAGE, "", null, null);

    assertEquals(Set.of(), criteria.tags());
    assertEquals(TagCombinationMode.DEFAULT, criteria.mode());
  }

  @Test
  void nothingAskedForIsAnEmptyQuestionRatherThanANullOne() {
    criteria.requested(null, null, Set.of(), TagCombinationMode.DEFAULT);

    assertEquals(ListingWindow.EMPTY, criteria.window());
    assertEquals("", criteria.search());
    assertFalse(criteria.isNewQuery(null));
    assertFalse(criteria.needsRequest(null, null, Set.of(), TagCombinationMode.DEFAULT));
  }
}
