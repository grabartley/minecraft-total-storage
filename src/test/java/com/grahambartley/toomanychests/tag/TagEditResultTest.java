package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagEditResultTest {
  @Test
  void aChangeCarriesHowMuchItTouched() {
    final TagEditResult result = TagEditResult.changed(12);
    assertTrue(result.isChange());
    assertEquals(12, result.affected());
    assertEquals(TagNameOutcome.OK, result.name());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -99})
  void aNegativeCountIsClampedRatherThanReported(int affected) {
    assertEquals(0, TagEditResult.changed(affected).affected());
  }

  @ParameterizedTest
  @EnumSource(value = TagNameOutcome.class, names = "OK", mode = EnumSource.Mode.EXCLUDE)
  void aRejectedNameKeepsWhyItWasRejected(TagNameOutcome why) {
    final TagEditResult result = TagEditResult.rejected(why);
    assertEquals(TagEditOutcome.NAME_REJECTED, result.outcome());
    assertEquals(why, result.name());
    assertFalse(result.isChange());
  }

  @ParameterizedTest
  @EnumSource(TagEditOutcome.class)
  void anOutcomeWithoutDetailCarriesNoCountAndNoNameComplaint(TagEditOutcome outcome) {
    final TagEditResult result = TagEditResult.of(outcome);
    assertEquals(outcome, result.outcome());
    assertEquals(0, result.affected());
    assertEquals(TagNameOutcome.OK, result.name());
  }

  @Test
  void aResultWithoutAnOutcomeIsRefused() {
    assertThrows(NullPointerException.class, () -> new TagEditResult(null, TagNameOutcome.OK, 0));
  }
}
