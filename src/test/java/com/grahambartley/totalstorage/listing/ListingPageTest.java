package com.grahambartley.totalstorage.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListingPageTest {
  private static final List<ListingRow<String>> ROWS =
      List.of(new ListingRow<>("a", 1L, 1), new ListingRow<>("b", 2L, 3));

  @Test
  void aPageCarriesTheRowsAndWhereTheySitInTheWholeResult() {
    ListingPage<String> page = new ListingPage<>(7L, ListingStatus.LIVE, 20, 500, ROWS);

    assertEquals(7L, page.revision());
    assertEquals(ListingStatus.LIVE, page.status());
    assertEquals(20, page.offset());
    assertEquals(500, page.totalMatching());
    assertEquals(ROWS, page.rows());
    assertEquals(2, page.size());
  }

  @DisplayName("counts that cannot exist are brought back to zero rather than carried")
  @ParameterizedTest(name = "offset {0} and total {1} become {2} and {3}")
  @CsvSource({"-1, -1, 0, 0", "-500, 4, 0, 4", "3, -8, 3, 0"})
  void clampsAnOffsetOrTotalBelowZero(
      int offset, int total, int expectedOffset, int expectedTotal) {
    ListingPage<String> page = new ListingPage<>(1L, ListingStatus.LIVE, offset, total, ROWS);

    assertEquals(expectedOffset, page.offset());
    assertEquals(expectedTotal, page.totalMatching());
  }

  @Test
  void aPageRefusesARevisionThatWasNeverIssued() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListingPage<>(-1L, ListingStatus.LIVE, 0, 0, ROWS));
  }

  @Test
  void aPageRefusesToBeBuiltWithoutRows() {
    assertThrows(
        NullPointerException.class,
        () -> new ListingPage<String>(1L, ListingStatus.LIVE, 0, 0, null));
  }

  @Test
  void aPageRefusesToBeBuiltWithoutSayingWhyItHoldsWhatItHolds() {
    assertThrows(NullPointerException.class, () -> new ListingPage<>(1L, null, 0, 0, ROWS));
  }

  @Test
  void aPendingPageIsWhatAScreenHoldsBeforeAnythingHasAnsweredIt() {
    ListingPage<String> page = ListingPage.pending(4L);

    assertEquals(4L, page.revision());
    assertEquals(ListingStatus.PENDING, page.status());
    assertEquals(0, page.offset());
    assertEquals(0, page.totalMatching());
    assertTrue(page.isEmpty());
  }

  @Test
  void anOfflinePageIsWhatATerminalRunningNoNetworkAnswersWith() {
    ListingPage<String> page = ListingPage.offline(4L);

    assertEquals(4L, page.revision());
    assertEquals(ListingStatus.OFFLINE, page.status());
    assertTrue(page.isEmpty());
  }

  @Test
  void theRowsAPageWasBuiltFromCannotBeChangedUnderIt() {
    List<ListingRow<String>> mutable = new ArrayList<>(ROWS);
    ListingPage<String> page = new ListingPage<>(1L, ListingStatus.LIVE, 0, 2, mutable);

    mutable.clear();

    assertEquals(2, page.size());
  }

  @Test
  void aRowIsFoundByWhereItSitsInTheWholeResultRatherThanInThePage() {
    ListingPage<String> page = new ListingPage<>(1L, ListingStatus.LIVE, 20, 500, ROWS);

    assertEquals("a", page.at(20).variant());
    assertEquals("b", page.at(21).variant());
  }

  @DisplayName("a position this page does not cover is a blank cell rather than a wrong one")
  @ParameterizedTest(name = "position {0}")
  @CsvSource({"0", "19", "22", "500", "-1"})
  void aPositionOutsideThePageIsAnsweredWithNothing(int index) {
    ListingPage<String> page = new ListingPage<>(1L, ListingStatus.LIVE, 20, 500, ROWS);

    assertNull(page.at(index));
  }
}
