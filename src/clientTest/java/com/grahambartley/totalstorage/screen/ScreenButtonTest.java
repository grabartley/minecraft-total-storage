package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ScreenButtonTest {
  @ParameterizedTest
  @EnumSource(ScreenButton.class)
  void everyButtonCarriesItsOwnSpriteSet(ScreenButton button) {
    assertNotNull(button.sprites());
    for (final ScreenButton other : ScreenButton.values()) {
      if (other != button) {
        assertNotEquals(button.sprites(), other.sprites());
      }
    }
  }

  @ParameterizedTest
  @EnumSource(ScreenButton.class)
  void everyButtonWearsADistinctSpriteWhenItIsUnavailable(ScreenButton button) {
    assertNotEquals(
        button.sprites().select(true, false, false), button.sprites().select(false, false, false));
  }

  @ParameterizedTest
  @EnumSource(ScreenButton.class)
  void everyButtonNamesItselfForTheTooltipAndTheNarrator(ScreenButton button) {
    assertNotNull(button.label());
    assertNotEquals("", button.label().getString());
  }
}
