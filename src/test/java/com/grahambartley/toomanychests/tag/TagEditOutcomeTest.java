package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TagEditOutcomeTest {
  @Test
  void onlyAChangeIsAChange() {
    assertTrue(TagEditOutcome.CHANGED.isChange());
  }

  @ParameterizedTest
  @EnumSource(value = TagEditOutcome.class, names = "CHANGED", mode = EnumSource.Mode.EXCLUDE)
  void everyOtherOutcomeChangedNothing(TagEditOutcome outcome) {
    assertFalse(outcome.isChange());
  }

  @ParameterizedTest
  @EnumSource(
      value = TagEditOutcome.class,
      names = {"CHANGED", "UNCHANGED"})
  void anEditThatWasAllowedIsNotARefusal(TagEditOutcome outcome) {
    assertFalse(outcome.isRefusal());
  }

  @ParameterizedTest
  @EnumSource(
      value = TagEditOutcome.class,
      names = {"CHANGED", "UNCHANGED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOtherOutcomeIsARefusalWorthReporting(TagEditOutcome outcome) {
    assertTrue(outcome.isRefusal());
  }
}
