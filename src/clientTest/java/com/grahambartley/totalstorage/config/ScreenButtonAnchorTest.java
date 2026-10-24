package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScreenButtonAnchorTest {
  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void everyAnchorAnswersToTheNameItWritesOut(ScreenButtonAnchor anchor) {
    assertEquals(Optional.of(anchor), ScreenButtonAnchor.fromId(anchor.id()));
  }

  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void anAnchorIsWrittenInLowerCase(ScreenButtonAnchor anchor) {
    assertEquals(anchor.id().toLowerCase(java.util.Locale.ROOT), anchor.id());
  }

  @ParameterizedTest
  @ValueSource(strings = {" INSIDE_TOP_RIGHT ", "inside_top_right", "Inside_Top_Right"})
  void aNameIsReadTheWayAPlayerTypedIt(String typed) {
    assertEquals(
        Optional.of(ScreenButtonAnchor.INSIDE_TOP_RIGHT), ScreenButtonAnchor.fromId(typed));
  }

  @Test
  void anAnchorNobodyShipsIsNotInvented() {
    assertTrue(ScreenButtonAnchor.fromId("middle_of_the_screen").isEmpty());
    assertTrue(ScreenButtonAnchor.fromId(null).isEmpty());
  }
}
