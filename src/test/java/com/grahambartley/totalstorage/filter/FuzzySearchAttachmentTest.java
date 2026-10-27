package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class FuzzySearchAttachmentTest {
  @Test
  void noPlayerMeansNoFuzzySearch() {
    assertFalse(FuzzySearchAttachment.get(null));
  }

  @Test
  void theChoiceIsStoredUnderTheModsOwnId() {
    assertEquals("total-storage:fuzzy_search", FuzzySearchAttachment.ID.toString());
    assertEquals(FuzzySearchAttachment.ID, FuzzySearchAttachment.TYPE.identifier());
  }
}
