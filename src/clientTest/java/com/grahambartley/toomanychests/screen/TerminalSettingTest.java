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

  @Test
  void onlyTheAccessModeChangesWhenItsRowIsClicked() {
    assertTrue(TerminalSetting.ACCESS.cyclesOnRowClick());
    assertFalse(TerminalSetting.RADIUS.cyclesOnRowClick());
    assertFalse(TerminalSetting.TRUSTED.cyclesOnRowClick());
  }

  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void onlyTheTrustListOpensAPromptWhenItsRowIsClicked(TerminalSetting setting) {
    assertEquals(setting == TerminalSetting.TRUSTED, setting.opensPromptOnRowClick());
  }

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

  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void everySettingHasALabel(TerminalSetting setting) {
    assertFalse(setting.label().isBlank());
  }
}
