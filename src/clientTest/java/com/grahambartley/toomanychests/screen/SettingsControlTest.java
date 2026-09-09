package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SettingsControlTest {
  static Stream<Arguments> bothSteppers() {
    return Stream.of(
        Arguments.of(SettingsControl.RADIUS_DOWN, SettingsControl.radiusDownX()),
        Arguments.of(SettingsControl.RADIUS_UP, SettingsControl.radiusUpX()));
  }

  @ParameterizedTest
  @MethodSource("bothSteppers")
  void aClickAnywhereOnAStepperReachesIt(SettingsControl control, int left) {
    int top = SettingsControl.radiusY();
    int size = SettingsControl.size();

    assertEquals(control, SettingsControl.at(left, top));
    assertEquals(control, SettingsControl.at(left + size - 1, top + size - 1));
  }

  @ParameterizedTest
  @MethodSource("bothSteppers")
  void aClickJustOutsideAStepperMissesIt(SettingsControl control, int left) {
    int top = SettingsControl.radiusY();
    int size = SettingsControl.size();

    assertFalse(SettingsControl.at(left - 1, top) == control);
    assertFalse(SettingsControl.at(left, top - 1) == control);
    assertFalse(SettingsControl.at(left + size, top) == control);
    assertFalse(SettingsControl.at(left, top + size) == control);
  }

  @Test
  void theSteppersSitAtOppositeEndsOfTheirRow() {
    assertEquals(
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.CONTROL_INSET,
        SettingsControl.radiusDownX());
    assertEquals(
        TerminalSettingsLayout.PANEL_X
            + TerminalSettingsLayout.FULL_ROW_WIDTH
            - TerminalSettingsLayout.CONTROL_INSET
            - SettingsControl.size(),
        SettingsControl.radiusUpX());
    assertTrue(
        SettingsControl.radiusDownX() + SettingsControl.size() < SettingsControl.radiusUpX(),
        "the steppers are close enough to be hit by mistake");
  }

  @Test
  void aStepperIsBigEnoughToAimAt() {
    assertTrue(
        SettingsControl.size() >= 16, "a control of " + SettingsControl.size() + " is too small");
  }

  @Test
  void aStepperSitsInsideItsOwnRow() {
    int rowTop = TerminalSettingsLayout.rowY(TerminalSetting.RADIUS.row());

    assertTrue(SettingsControl.radiusY() >= rowTop);
    assertTrue(
        SettingsControl.radiusY() + SettingsControl.size()
            <= rowTop + TerminalSettingsLayout.ROW_HEIGHT);
  }

  @Test
  void aStepperReachesTheEdgeOfThePanelRatherThanTheGutter() {
    assertEquals(
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.PANEL_WIDTH,
        SettingsControl.radiusUpX()
            + SettingsControl.size()
            + TerminalSettingsLayout.CONTROL_INSET);
  }

  @Test
  void aClickOnTheEmptyMiddleOfTheRowIsNotAStepper() {
    assertEquals(
        SettingsControl.NONE,
        SettingsControl.at(
            TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.ROW_WIDTH / 2,
            SettingsControl.radiusY()));
    assertFalse(SettingsControl.NONE.isSomething());
    assertTrue(SettingsControl.RADIUS_UP.isSomething());
  }

  @Test
  void theStepperThatRaisesTheRadiusReachesIntoTheScrollbarsBand() {
    int rightmost = SettingsControl.radiusUpX() + SettingsControl.size() - 1;

    assertTrue(
        TerminalSettingsLayout.isOverScrollbar(rightmost, SettingsControl.radiusY()),
        "the stepper no longer overlaps the gutter, so this case has nothing left to pin");
    assertEquals(
        SettingsControl.RADIUS_UP, SettingsControl.at(rightmost, SettingsControl.radiusY()));
  }

  @Test
  void aClickOnAnotherSettingsRowIsNotAStepper() {
    assertEquals(
        SettingsControl.NONE,
        SettingsControl.at(
            SettingsControl.radiusUpX(),
            TerminalSettingsLayout.rowY(TerminalSetting.ACCESS.row())));
  }
}
