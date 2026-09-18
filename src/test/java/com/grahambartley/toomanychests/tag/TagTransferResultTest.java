package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagTransferResultTest {
  @Test
  void aTransferThatStoredSomethingReportsWhatItBroughtAndWhatItSkipped() {
    final TagTransferResult result =
        TagTransferResult.from(
            new TagSetImportResult(
                NetworkTagSet.defaults(), 3, TagSkips.of(TagSkipReason.TOO_LONG, 2)),
            true);

    assertTrue(result.isChange());
    assertEquals(3, result.imported());
    assertEquals(2, result.skipped());
    assertEquals(2, result.skips().countOf(TagSkipReason.TOO_LONG));
  }

  @Test
  void aTransferThatChangedNothingStillReportsWhatItSkipped() {
    final TagTransferResult result =
        TagTransferResult.from(
            new TagSetImportResult(
                NetworkTagSet.defaults(), 0, TagSkips.of(TagSkipReason.MALFORMED, 4)),
            false);

    assertFalse(result.isChange());
    assertEquals(0, result.imported());
    assertEquals(4, result.skipped());
    assertEquals(4, result.skips().countOf(TagSkipReason.MALFORMED));
  }

  @Test
  void aRefusalCarriesNoCounts() {
    final TagTransferResult refused = TagTransferResult.of(TagEditOutcome.DENIED);

    assertFalse(refused.isChange());
    assertEquals(0, refused.imported());
    assertEquals(0, refused.skipped());
  }
}
