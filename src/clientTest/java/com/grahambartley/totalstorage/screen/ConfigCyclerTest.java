package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ClientPreferences;
import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.ConfigSettings;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigCyclerTest {
  private static final ClientPreferences SAVED =
      new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, true);

  private static CyclingButtonWidget<String> preferenceCycler(ClientPreference preference) {
    return ConfigCycler.of(
        preference,
        new ConfigDraft<>(SAVED),
        preference.choices(),
        value -> ClientPreferenceText.displayValue(preference, value),
        ClientPreferenceText.label(preference));
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void aCyclerOpensOnTheDraftsValueInTheOptionsOwnWords(ClientPreference preference) {
    final CyclingButtonWidget<String> cycler = preferenceCycler(preference);

    assertEquals(preference.valueIn(SAVED), cycler.getValue());
    assertEquals(
        ClientPreferenceText.displayValue(preference, preference.valueIn(SAVED)).getString(),
        cycler.getMessage().getString());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void aCyclerFillsTheRowsControlSlot(ClientPreference preference) {
    final CyclingButtonWidget<String> cycler = preferenceCycler(preference);

    assertEquals(ConfigRow.CONTROL_WIDTH, cycler.getWidth());
    assertEquals(ConfigRow.CONTROL_HEIGHT, cycler.getHeight());
  }

  @ParameterizedTest
  @ValueSource(strings = {"sortOrdering", "sortButtonEnabled", "defaultTerminalAccessMode"})
  void aServerSettingCyclesThroughTheSameFactory(String name) {
    final ConfigSetting setting = ConfigSettings.byName(name).orElseThrow();
    final CyclingButtonWidget<String> cycler =
        ConfigCycler.of(
            setting,
            new ConfigDraft<>(TotalStorageConfig.defaults()),
            setting.suggestions(),
            setting::displayValue,
            Text.literal(name));

    assertEquals(setting.defaultValue(), cycler.getValue());
    assertEquals(
        setting.displayValue(setting.defaultValue()).getString(), cycler.getMessage().getString());
  }
}
