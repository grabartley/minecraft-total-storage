package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FuzzySearchButtonTest {
  private final List<Boolean> sent = new ArrayList<>();

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void clickingAsksTheServerForTheOtherState(boolean enabled) {
    final FuzzySearchButton button = new FuzzySearchButton(0, 0, enabled, sent::add);

    button.onClick(0, 0);

    assertEquals(List.of(!enabled), sent);
    assertEquals(enabled, button.isOn());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void theButtonFollowsTheStateTheServerSyncedBack(boolean enabled) {
    final FuzzySearchButton button = new FuzzySearchButton(0, 0, !enabled, sent::add);

    button.update(true, enabled);

    assertEquals(enabled, button.isOn());
    assertEquals(FuzzySearchButton.label(enabled).getString(), button.getMessage().getString());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void theButtonHidesWithTheSearchBox(boolean shown) {
    final FuzzySearchButton button = new FuzzySearchButton(0, 0, false, sent::add);

    button.update(shown, false);

    assertEquals(shown, button.visible);
  }

  @ParameterizedTest
  @CsvSource({"0,true,true,true", "1,true,true,false", "0,false,true,false", "0,true,false,false"})
  void onlyALeftClickOnTheShownButtonIsTakenByIt(
      int mouseButton, boolean shown, boolean over, boolean taken) {
    final FuzzySearchButton button = new FuzzySearchButton(0, 0, false, sent::add);
    button.update(shown, false);
    final double x = over ? button.getX() + 1 : button.getX() - 1;

    assertEquals(taken, button.takesClick(x, button.getY() + 1, mouseButton));
  }

  @Test
  void onAndOffReadAndLookDifferent() {
    assertNotEquals(
        FuzzySearchButton.label(true).getString(), FuzzySearchButton.label(false).getString());
    assertNotEquals(FuzzySearchButton.icon(true), FuzzySearchButton.icon(false));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void theTooltipNamesTheStateAndExplainsIt(boolean enabled) {
    final String tooltip = FuzzySearchButton.tooltip(enabled).getString();

    assertTrue(tooltip.startsWith(FuzzySearchButton.label(enabled).getString()));
    assertTrue(tooltip.contains("\n"));
  }

  @Test
  void theButtonSitsAtTheEndOfTheSearchBox() {
    final FuzzySearchButton button = new FuzzySearchButton(100, 50, false, sent::add);

    assertEquals(100 + TerminalScreenLayout.FUZZY_X, button.getX());
    assertEquals(50 + TerminalScreenLayout.FUZZY_Y, button.getY());
    assertEquals(TerminalScreenLayout.FUZZY_SIZE, button.getWidth());
    assertFalse(button.isOn());
  }
}
