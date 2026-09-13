package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuickStackButtonSpriteTest {
  @ParameterizedTest
  @CsvSource({
    "true, false, false, QUICK_STACK",
    "true, true, false, QUICK_STACK_HOVERED",
    "true, true, true, QUICK_STACK_PRESSED",
    "true, false, true, QUICK_STACK_PRESSED",
    "false, false, false, QUICK_STACK_UNAVAILABLE",
    "false, true, true, QUICK_STACK_UNAVAILABLE"
  })
  void everyStateOfTheButtonWearsItsOwnSprite(
      boolean available, boolean hovered, boolean held, String expected) {
    assertEquals(
        TerminalSprite.valueOf(expected), QuickStackButtonSprite.of(available, hovered, held));
  }

  @Test
  void anUnavailableButtonNeverLooksLikeAnAvailableOne() {
    assertNotEquals(
        QuickStackButtonSprite.of(true, false, false),
        QuickStackButtonSprite.of(false, false, false));
  }
}
