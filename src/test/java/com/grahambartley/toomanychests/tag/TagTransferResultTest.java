package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagTransferResultTest {
  @Test
  void aTransferThatStoredSomethingReportsWhatItBroughtAndWhatItSkipped() {
    final TagTransferResult result =
        TagTransferResult.from(new TagSetImportResult(NetworkTagSet.defaults(), 3, 2), true);

    assertTrue(result.isChange());
    assertEquals(3, result.imported());
    assertEquals(2, result.skipped());
  }

  @Test
  void aTransferThatChangedNothingStillReportsWhatItSkipped() {
    final TagTransferResult result =
        TagTransferResult.from(new TagSetImportResult(NetworkTagSet.defaults(), 0, 4), false);

    assertFalse(result.isChange());
    assertEquals(0, result.imported());
    assertEquals(4, result.skipped());
  }

  @Test
  void aRefusalCarriesNoCounts() {
    final TagTransferResult refused = TagTransferResult.of(TagEditOutcome.DENIED);

    assertFalse(refused.isChange());
    assertEquals(0, refused.imported());
    assertEquals(0, refused.skipped());
  }
}
