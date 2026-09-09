package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FilterColumnClickTest {
  @ParameterizedTest
  @EnumSource(value = FilterColumnClick.class, names = "IGNORED", mode = EnumSource.Mode.EXCLUDE)
  void aClickTheColumnTookIsNotPassedOn(FilterColumnClick click) {
    assertTrue(click.isHandled());
  }

  @Test
  void aClickTheColumnDidNotWantIsPassedOn() {
    assertFalse(FilterColumnClick.IGNORED.isHandled());
  }

  @Test
  void onlyAChangedFilterIsWorthAskingTheServerAgain() {
    assertTrue(FilterColumnClick.FILTER_CHANGED.changedFilter());
    assertFalse(FilterColumnClick.HANDLED.changedFilter());
    assertFalse(FilterColumnClick.SCROLLBAR_GRABBED.changedFilter());
    assertFalse(FilterColumnClick.IGNORED.changedFilter());
  }
}
