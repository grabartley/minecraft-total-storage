package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagSetImportResultTest {
  @Test
  void anImportThatBroughtTagsAcrossSaysSo() {
    assertTrue(
        new TagSetImportResult(NetworkTagSet.defaults(), 1, TagSkips.NONE)
            .broughtSomethingAcross());
  }

  @Test
  void anImportThatBroughtNothingSaysSo() {
    assertFalse(
        new TagSetImportResult(NetworkTagSet.defaults(), 0, TagSkips.of(TagSkipReason.MALFORMED, 3))
            .broughtSomethingAcross());
  }

  @Test
  void negativeCountsAreNeverReportedBack() {
    final TagSetImportResult result =
        new TagSetImportResult(
            NetworkTagSet.defaults(), -1, TagSkips.of(TagSkipReason.MALFORMED, -2));

    assertEquals(0, result.imported());
    assertEquals(0, result.skipped());
  }

  @Test
  void anImportWithNoSkipTallyStillAnswersForOne() {
    assertEquals(0, new TagSetImportResult(NetworkTagSet.defaults(), 1, null).skipped());
  }

  @Test
  void theSkippedCountIsTheSumOfEveryReason() {
    final TagSetImportResult result =
        new TagSetImportResult(
            NetworkTagSet.defaults(),
            1,
            TagSkips.of(TagSkipReason.MALFORMED, 2).and(TagSkipReason.TOO_LONG));

    assertEquals(3, result.skipped());
  }
}
