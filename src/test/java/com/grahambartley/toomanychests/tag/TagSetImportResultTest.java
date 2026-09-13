package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagSetImportResultTest {
  @Test
  void anImportThatBroughtTagsAcrossSaysSo() {
    assertTrue(new TagSetImportResult(NetworkTagSet.defaults(), 1, 0).broughtSomethingAcross());
  }

  @Test
  void anImportThatBroughtNothingSaysSo() {
    assertFalse(new TagSetImportResult(NetworkTagSet.defaults(), 0, 3).broughtSomethingAcross());
  }

  @Test
  void negativeCountsAreNeverReportedBack() {
    final TagSetImportResult result = new TagSetImportResult(NetworkTagSet.defaults(), -1, -2);

    assertEquals(0, result.imported());
    assertEquals(0, result.skipped());
  }
}
