package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SettingsControlTest {
  private static final int SIZE = TerminalScreenLayout.SETTINGS_CONTROL_SIZE;

  static Stream<Arguments> everyControl() {
    return Stream.of(
        Arguments.of(
            SettingsControl.RADIUS_DOWN,
            SettingsControl.radiusDownX(),
            SettingsControl.rowY(SettingsControl.radiusRow())),
        Arguments.of(
            SettingsControl.RADIUS_UP,
            SettingsControl.radiusUpX(),
            SettingsControl.rowY(SettingsControl.radiusRow())),
        Arguments.of(
            SettingsControl.ACCESS_CYCLE,
            SettingsControl.accessCycleX(),
            SettingsControl.rowY(SettingsControl.accessRow())));
  }

  @ParameterizedTest
  @MethodSource("everyControl")
  void aClickOnAControlReachesIt(SettingsControl control, int left, int top) {
    assertEquals(control, SettingsControl.at(left, top));
    assertEquals(control, SettingsControl.at(left + SIZE - 1, top + SIZE - 1));
  }

  @ParameterizedTest
  @MethodSource("everyControl")
  void aClickJustOutsideAControlMissesIt(SettingsControl control, int left, int top) {
    assertFalse(SettingsControl.at(left - 1, top) == control);
    assertFalse(SettingsControl.at(left, top - 1) == control);
    assertFalse(SettingsControl.at(left + SIZE, top) == control);
    assertFalse(SettingsControl.at(left, top + SIZE) == control);
  }

  /** Two controls sharing a pixel would make one of them unreachable. */
  @Test
  void theRadiusControlsDoNotOverlap() {
    assertTrue(
        SettingsControl.radiusDownX() + SIZE <= SettingsControl.radiusUpX(),
        "the radius buttons run into each other");
  }

  @Test
  void everyControlSitsInsideThePanelItIsDrawnOn() {
    final int right = TerminalScreenLayout.SETTINGS_X + TerminalScreenLayout.SETTINGS_WIDTH;
    everyControl()
        .forEach(
            arguments -> {
              int left = (int) arguments.get()[1];
              int top = (int) arguments.get()[2];
              assertTrue(left >= TerminalScreenLayout.SETTINGS_X, "control at " + left);
              assertTrue(left + SIZE <= right, "control ends at " + (left + SIZE));
              assertTrue(top >= TerminalScreenLayout.SETTINGS_Y, "control at " + top);
            });
  }

  @Test
  void aClickOnTheEmptyPartOfTheHeaderIsNotAControl() {
    assertEquals(
        SettingsControl.NONE,
        SettingsControl.at(TerminalScreenLayout.SETTINGS_X, SettingsControl.rowY(0)));
    assertFalse(SettingsControl.NONE.isSomething());
    assertTrue(SettingsControl.RADIUS_UP.isSomething());
  }

  /** The container list is a different surface, so a click there must not hit a header control. */
  @Test
  void aClickOnTheContainerListIsNotAControl() {
    assertEquals(
        SettingsControl.NONE,
        SettingsControl.at(SettingsControl.radiusUpX(), TerminalScreenLayout.CONTAINER_LIST_Y + 1));
  }
}
