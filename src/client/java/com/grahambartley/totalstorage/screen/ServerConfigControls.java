package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ConfigRange;
import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;

public final class ServerConfigControls {
  private ServerConfigControls() {}

  public static ClickableWidget of(
      ConfigSetting setting, ConfigDraft<TotalStorageConfig> edits, boolean editable) {
    final ClickableWidget control =
        editable
            ? setting
                .range()
                .<ClickableWidget>map(range -> slider(setting, edits, range))
                .orElseGet(() -> cycler(setting, edits))
            : readOnly(setting, edits);
    control.active = editable;
    return control;
  }

  private static ClickableWidget readOnly(
      ConfigSetting setting, ConfigDraft<TotalStorageConfig> edits) {
    return ButtonWidget.builder(setting.displayValue(edits.valueOf(setting)), button -> {})
        .dimensions(0, 0, ConfigRow.CONTROL_WIDTH, ConfigRow.CONTROL_HEIGHT)
        .build();
  }

  private static ClickableWidget slider(
      ConfigSetting setting, ConfigDraft<TotalStorageConfig> edits, ConfigRange range) {
    return new SettingSlider(setting, edits, range);
  }

  private static ClickableWidget cycler(
      ConfigSetting setting, ConfigDraft<TotalStorageConfig> edits) {
    return ConfigCycler.of(
        setting,
        edits,
        setting.suggestions(),
        setting::displayValue,
        ServerConfigText.label(setting));
  }

  private static final class SettingSlider extends SliderWidget {
    private final ConfigSetting setting;
    private final ConfigDraft<TotalStorageConfig> edits;
    private final ConfigRange range;

    private SettingSlider(
        ConfigSetting setting, ConfigDraft<TotalStorageConfig> edits, ConfigRange range) {
      super(
          0,
          0,
          ConfigRow.CONTROL_WIDTH,
          ConfigRow.CONTROL_HEIGHT,
          setting.displayValue(edits.valueOf(setting)),
          range.fractionOf(Integer.parseInt(edits.valueOf(setting))));
      this.setting = setting;
      this.edits = edits;
      this.range = range;
    }

    @Override
    protected void updateMessage() {
      setMessage(setting.displayValue(Integer.toString(range.valueAt(value))));
    }

    @Override
    protected void applyValue() {
      edits.set(setting, Integer.toString(range.valueAt(value)));
    }
  }
}
