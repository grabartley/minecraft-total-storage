package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalSettingTest {

  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void everySettingIsFoundAtTheRowItIsDrawnOn(TerminalSetting setting) {
    assertEquals(setting, TerminalSetting.atRow(setting.row()));
  }

  @Test
  void theSettingsAreNumberedInTheOrderTheyAreDrawn() {
    assertEquals(0, TerminalSetting.RADIUS.row());
    assertEquals(1, TerminalSetting.ACCESS.row());
    assertEquals(2, TerminalSetting.TRUSTED.row());
  }

  /** The terminal section has to fit in the rows the panel gives a section. */
  @Test
  void everySettingHasARowToBeDrawnOn() {
    assertTrue(
        TerminalSetting.values().length <= TerminalSettingsLayout.ROWS,
        TerminalSetting.values().length
            + " settings do not fit "
            + TerminalSettingsLayout.ROWS
            + " rows");
  }

  @Test
  void aRowNoSettingIsDrawnOnHoldsNothing() {
    assertNull(TerminalSetting.atRow(-1));
    assertNull(TerminalSetting.atRow(TerminalSetting.values().length));
  }

  /**
   * Only the access mode cycles on a row click. The radius carries its direction in two steppers,
   * and the trust list is read here and edited from the command line.
   */
  @Test
  void onlyTheAccessModeChangesWhenItsRowIsClicked() {
    assertTrue(TerminalSetting.ACCESS.cyclesOnRowClick());
    assertFalse(TerminalSetting.RADIUS.cyclesOnRowClick());
    assertFalse(TerminalSetting.TRUSTED.cyclesOnRowClick());
  }

  /**
   * The keys are written out rather than derived, because these are what the panel draws through
   * and a lang file cannot follow a renamed enum constant. A test that recomputes the derivation
   * would agree with a rename that had quietly changed what a player reads.
   */
  @ParameterizedTest
  @CsvSource({
    "RADIUS,settings.radius",
    "ACCESS,settings.access",
    "TRUSTED,settings.trusted",
    "CAPACITY,settings.capacity"
  })
  void everySettingIsNamedUnderTheKeyTheLangFileCarries(TerminalSetting setting, String key) {
    assertEquals(key, setting.label());
  }

  /** Every row of the section has a name, so none of them can be drawn without one. */
  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void everySettingHasALabel(TerminalSetting setting) {
    assertFalse(setting.label().isBlank());
  }
}
