package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SortReplyTest {
  @Test
  void aReplyWithoutASubjectIsRejected() {
    assertThrows(NullPointerException.class, () -> new SortReply(null, SortOutcome.SORTED));
  }

  @Test
  void aReplyWithoutAnOutcomeIsRejected() {
    assertThrows(NullPointerException.class, () -> new SortReply(SortSubject.INVENTORY, null));
  }
}
