package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class IconButtonSpritesTest {
  @ParameterizedTest
  @CsvSource({
    "true, false, false, QUICK_STACK",
    "true, true, false, QUICK_STACK_HOVERED",
    "true, true, true, QUICK_STACK_PRESSED",
    "true, false, true, QUICK_STACK_PRESSED",
    "false, false, false, QUICK_STACK_UNAVAILABLE",
    "false, true, true, QUICK_STACK_UNAVAILABLE"
  })
  void everyStateOfTheQuickStackButtonWearsItsOwnSprite(
      boolean available, boolean hovered, boolean held, String expected) {
    assertEquals(
        TerminalSprite.valueOf(expected),
        IconButtonSprites.QUICK_STACK.select(available, hovered, held));
  }

  @ParameterizedTest
  @CsvSource({
    "true, false, false, SORT",
    "true, true, false, SORT_HOVERED",
    "true, true, true, SORT_PRESSED",
    "true, false, true, SORT_PRESSED",
    "false, false, false, SORT_UNAVAILABLE",
    "false, true, true, SORT_UNAVAILABLE"
  })
  void everyStateOfTheSortButtonWearsItsOwnSprite(
      boolean available, boolean hovered, boolean held, String expected) {
    assertEquals(
        TerminalSprite.valueOf(expected), IconButtonSprites.SORT.select(available, hovered, held));
  }

  @ParameterizedTest
  @EnumSource(ScreenButton.class)
  void anUnavailableButtonNeverLooksLikeAnAvailableOne(ScreenButton button) {
    assertNotEquals(
        button.sprites().select(true, false, false), button.sprites().select(false, false, false));
  }

  @ParameterizedTest
  @EnumSource(ScreenButton.class)
  void noTwoButtonsShareASprite(ScreenButton button) {
    for (final ScreenButton other : ScreenButton.values()) {
      if (other != button) {
        assertNotEquals(
            button.sprites().select(true, false, false),
            other.sprites().select(true, false, false));
      }
    }
  }

  @ParameterizedTest
  @CsvSource({"0", "1", "2", "3"})
  void aSpriteSetWithAHoleInItIsRefused(int missing) {
    final TerminalSprite[] sprites = {
      TerminalSprite.SORT,
      TerminalSprite.SORT_HOVERED,
      TerminalSprite.SORT_PRESSED,
      TerminalSprite.SORT_UNAVAILABLE
    };
    sprites[missing] = null;

    assertThrows(
        NullPointerException.class,
        () -> new IconButtonSprites(sprites[0], sprites[1], sprites[2], sprites[3]));
  }
}
