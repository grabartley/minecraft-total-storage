package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.ConfigSettings;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ServerConfigControlsTest {
  private static ClickableWidget control(String name, boolean editable) {
    final ConfigSetting setting = ConfigSettings.byName(name).orElseThrow();
    return ServerConfigControls.of(
        setting, new ConfigDraft<>(TotalStorageConfig.defaults()), editable);
  }

  @ParameterizedTest
  @CsvSource({
    "maxTerminalRadius, true",
    "nearCapacityWarningPercent, true",
    "sortOrdering, false",
    "quickStackEnabled, false"
  })
  void anEditorGetsASliderForANumberAndACyclerForAChoice(String name, boolean ranged) {
    final ClickableWidget built = control(name, true);

    final Class<? extends ClickableWidget> expected =
        ranged ? SliderWidget.class : CyclingButtonWidget.class;
    assertInstanceOf(expected, built);
    assertEquals(true, built.active);
  }

  @ParameterizedTest
  @ValueSource(strings = {"maxTerminalRadius", "sortOrdering", "quickStackEnabled"})
  void aReadOnlyPlayerSeesTheValueOnAnInertButton(String name) {
    final ConfigSetting setting = ConfigSettings.byName(name).orElseThrow();
    final ClickableWidget built = control(name, false);

    assertInstanceOf(ButtonWidget.class, built);
    assertEquals(false, built.active);
    assertEquals(
        setting.displayValue(setting.defaultValue()).getString(), built.getMessage().getString());
  }

  @ParameterizedTest
  @CsvSource({"maxTerminalRadius, true", "sortOrdering, true", "sortOrdering, false"})
  void everyControlFillsTheRowsControlSlot(String name, boolean editable) {
    final ClickableWidget built = control(name, editable);

    assertEquals(ConfigRow.CONTROL_WIDTH, built.getWidth());
    assertEquals(ConfigRow.CONTROL_HEIGHT, built.getHeight());
  }
}
