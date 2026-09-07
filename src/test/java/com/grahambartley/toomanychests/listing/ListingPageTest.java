package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
      List.of(new ListingRow<>("a", 1L), new ListingRow<>("b", 2L));

  @Test
  void aPageCarriesTheRowsAndWhereTheySitInTheWholeResult() {
    ListingPage<String> page = new ListingPage<>(7L, 20, 500, ROWS);

    assertEquals(7L, page.revision());
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
    ListingPage<String> page = new ListingPage<>(1L, offset, total, ROWS);

    assertEquals(expectedOffset, page.offset());
    assertEquals(expectedTotal, page.totalMatching());
  }

  @Test
  void aPageRefusesARevisionThatWasNeverIssued() {
    assertThrows(IllegalArgumentException.class, () -> new ListingPage<>(-1L, 0, 0, ROWS));
  }

  @Test
  void aPageRefusesToBeBuiltWithoutRows() {
    assertThrows(NullPointerException.class, () -> new ListingPage<String>(1L, 0, 0, null));
  }

  @Test
  void anEmptyPageIsWhatATerminalRunningNoNetworkAnswersWith() {
    ListingPage<String> page = ListingPage.empty(4L);

    assertEquals(4L, page.revision());
    assertEquals(0, page.offset());
    assertEquals(0, page.totalMatching());
    assertTrue(page.isEmpty());
  }

  @Test
  void theRowsAPageWasBuiltFromCannotBeChangedUnderIt() {
    List<ListingRow<String>> mutable = new ArrayList<>(ROWS);
    ListingPage<String> page = new ListingPage<>(1L, 0, 2, mutable);

    mutable.clear();

    assertEquals(2, page.size());
  }
}
