package com.grahambartley.totalstorage.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ListingRevisionTest {
  private final ListingRevision revision = new ListingRevision();

  @Test
  void anIssuerThatHasStampedNothingIsAtTheStartingPoint() {
    assertEquals(ListingRevision.NONE, revision.current());
  }

  @Test
  void everyPageIsStampedNewerThanTheOneBeforeIt() {
    assertEquals(1L, revision.next());
    assertEquals(2L, revision.next());
    assertEquals(3L, revision.next());
    assertEquals(3L, revision.current());
  }

  @Test
  void theFirstPageIssuedBeatsHoldingNoPageAtAll() {
    assertTrue(ListingRevision.supersedes(revision.next(), ListingRevision.NONE));
  }

  @Test
  void aNewerPageSupersedesTheOneBeingShown() {
    assertTrue(ListingRevision.supersedes(9L, 8L));
  }

  @Test
  void aPageThatIsNotNewerDoesNotSupersedeAnything() {
    assertFalse(ListingRevision.supersedes(8L, 8L));
    assertFalse(ListingRevision.supersedes(7L, 8L));
  }
}
