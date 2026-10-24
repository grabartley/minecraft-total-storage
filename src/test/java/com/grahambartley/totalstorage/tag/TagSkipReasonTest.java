package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TagSkipReasonTest {
  @ParameterizedTest
  @CsvSource({
    "TOO_LONG, TOO_LONG",
    "PRESET_COLLISION, PRESET_NAME",
    "BLANK, MALFORMED",
    "ILLEGAL_CHARACTER, ILLEGAL_NAME"
  })
  void everyRejectedNameLandsUnderAReasonTheSummaryCanName(
      TagNameOutcome outcome, TagSkipReason expected) {
    assertEquals(expected, TagSkipReason.of(outcome));
  }

  @ParameterizedTest
  @EnumSource(
      value = TagNameOutcome.class,
      names = {"OK", "DUPLICATE"})
  void anOutcomeThatIsNotASkipHasNoReason(TagNameOutcome outcome) {
    assertThrows(IllegalArgumentException.class, () -> TagSkipReason.of(outcome));
  }

  @ParameterizedTest
  @EnumSource(TagSkipReason.class)
  void everyReasonCarriesAnIdForItsTranslationKey(TagSkipReason reason) {
    assertFalse(reason.id().isBlank());
    assertEquals(reason.id().trim(), reason.id());
  }

  @Test
  void noTwoReasonsShareAnId() {
    final Set<String> ids = new HashSet<>();
    for (final TagSkipReason reason : TagSkipReason.values()) {
      assertTrue(ids.add(reason.id()), reason + " reuses an id");
    }
  }
}
