package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SettingsClickTest {
  private static final boolean OVER_SOMETHING = true;
  private static final boolean OVER_NOTHING = false;
  private static final boolean MAY = true;
  private static final boolean MAY_NOT = false;

  @Test
  void leftAndRightWalkTheRoleInOppositeDirections() {
    assertEquals(
        SettingsAction.CYCLE_FORWARD, SettingsClick.of(GridClick.LEFT_BUTTON, OVER_SOMETHING, MAY));
    assertEquals(
        SettingsAction.CYCLE_BACK, SettingsClick.of(GridClick.RIGHT_BUTTON, OVER_SOMETHING, MAY));
  }

  @ParameterizedTest
  @ValueSource(ints = {GridClick.MIDDLE_BUTTON, 3, 7})
  void aButtonWithNoMeaningHereIsHandedBack(int button) {
    assertEquals(SettingsAction.NONE, SettingsClick.of(button, OVER_SOMETHING, MAY));
  }

  @ParameterizedTest
  @ValueSource(ints = {GridClick.LEFT_BUTTON, GridClick.RIGHT_BUTTON})
  void aClickOnNothingThisPanelOwnsIsHandedBack(int button) {
    assertEquals(SettingsAction.NONE, SettingsClick.of(button, OVER_NOTHING, MAY));
  }

  @ParameterizedTest
  @ValueSource(ints = {GridClick.LEFT_BUTTON, GridClick.RIGHT_BUTTON})
  void aPlayerWhoMayNotChangeAnythingStillSwallowsTheClick(int button) {
    assertEquals(SettingsAction.REFUSED, SettingsClick.of(button, OVER_SOMETHING, MAY_NOT));
  }

  @Test
  void aButtonWithNoMeaningIsHandedBackEvenForSomebodyWhoMayNotChangeAnything() {
    assertEquals(
        SettingsAction.NONE, SettingsClick.of(GridClick.MIDDLE_BUTTON, OVER_SOMETHING, MAY_NOT));
  }
}
