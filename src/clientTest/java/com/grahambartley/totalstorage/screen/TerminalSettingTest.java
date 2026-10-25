package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalSettingTest {
  @Test
  void theSettingsAreDrawnInTheOrderThePanelLaysThemOut() {
    assertEquals(0, TerminalSetting.ACCESS.row());
    assertEquals(1, TerminalSetting.TRUSTED.row());
    assertEquals(2, TerminalSetting.RADIUS.row());
  }

  @Test
  void everySettingHasARowToBeDrawnOn() {
    assertEquals(TerminalSettingsLayout.SETTING_COUNT, TerminalSetting.values().length);
  }

  @ParameterizedTest
  @CsvSource({"ACCESS,settings.access", "TRUSTED,settings.trusted", "RADIUS,settings.radius"})
  void everySettingIsNamedAndDescribedUnderItsOwnKeys(TerminalSetting setting, String key) {
    assertEquals(key + ".label", setting.label());
    assertEquals(key + ".description", setting.description());
  }

  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void aLabelAndItsDescriptionAreDifferentLines(TerminalSetting setting) {
    assertNotEquals(setting.label(), setting.description());
  }
}
