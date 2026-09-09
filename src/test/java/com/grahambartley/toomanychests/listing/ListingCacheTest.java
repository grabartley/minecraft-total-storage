package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ListingCacheTest {
  private final ListingCache<String> cache = new ListingCache<>();

  private static ListingPage<String> page(long revision, String variant) {
    return new ListingPage<>(
        revision, ListingStatus.LIVE, 0, 1, List.of(new ListingRow<>(variant, 1L, 1)));
  }

  @Test
  void aCacheNobodyHasSentAPageToIsShowingNothing() {
    assertTrue(cache.page().isEmpty());
    assertEquals(ListingRevision.NONE, cache.page().revision());
    assertEquals(List.of(), cache.page().rows());
    assertEquals(0, cache.page().totalMatching());
  }

  @Test
  void theFirstPageToArriveIsTaken() {
    assertTrue(cache.accept(page(1L, "cobblestone")));

    assertEquals(1L, cache.page().revision());
    assertEquals("cobblestone", cache.page().rows().get(0).variant());
  }

  @Test
  void aNewerPageReplacesTheOneBeingShown() {
    cache.accept(page(1L, "cobblestone"));

    assertTrue(cache.accept(page(2L, "oak_log")));
    assertEquals("oak_log", cache.page().rows().get(0).variant());
  }

  @DisplayName("a page no newer than the one being shown is dropped rather than drawn")
  @ParameterizedTest(name = "revision {0} arriving after revision 5")
  @ValueSource(longs = {1L, 4L, 5L})
  void dropsAPageThatANewerOneHasAlreadyOvertaken(long arriving) {
    cache.accept(page(5L, "cobblestone"));

    assertFalse(cache.accept(page(arriving, "oak_log")));
    assertEquals(5L, cache.page().revision());
    assertEquals("cobblestone", cache.page().rows().get(0).variant());
  }

  @Test
  void aPageCarriesWhereItSitsAndHowMuchThereIsToScrollThrough() {
    cache.accept(
        new ListingPage<>(
            1L, ListingStatus.LIVE, 40, 500, List.of(new ListingRow<>("cobblestone", 8L, 2))));

    assertEquals(40, cache.page().offset());
    assertEquals(500, cache.page().totalMatching());
  }

  @Test
  void clearingLetsAFreshScreenStartFromTheBeginningAgain() {
    cache.accept(page(9L, "cobblestone"));

    cache.clear();

    assertTrue(cache.page().isEmpty());
    assertEquals(ListingRevision.NONE, cache.page().revision());
    assertTrue(cache.accept(page(1L, "oak_log")));
  }

  @Test
  void aCacheRefusesAPageThatIsNotThere() {
    assertThrows(NullPointerException.class, () -> cache.accept(null));
  }
}
