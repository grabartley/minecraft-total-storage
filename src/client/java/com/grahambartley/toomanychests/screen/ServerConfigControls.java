package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigRange;
import com.grahambartley.toomanychests.config.ConfigSetting;
import java.util.List;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;

public final class ServerConfigControls {
  public static final int WIDTH = 100;
  public static final int HEIGHT = 20;

  private ServerConfigControls() {}

  public static ClickableWidget of(ConfigSetting setting, ServerConfigEdits edits) {
    return setting
        .range()
        .<ClickableWidget>map(range -> slider(setting, edits, range))
        .orElseGet(() -> cycler(setting, edits));
  }

  private static ClickableWidget slider(
      ConfigSetting setting, ServerConfigEdits edits, ConfigRange range) {
    return new SettingSlider(setting, edits, range);
  }

  private static ClickableWidget cycler(ConfigSetting setting, ServerConfigEdits edits) {
    final List<String> alternatives = setting.suggestions();
    return CyclingButtonWidget.<String>builder(ServerConfigText::value)
        .values(alternatives)
        .initially(edits.valueOf(setting))
        .omitKeyText()
        .build(
            0,
            0,
            WIDTH,
            HEIGHT,
            ServerConfigText.label(setting),
            (button, chosen) -> edits.set(setting, chosen));
  }

  private static final class SettingSlider extends SliderWidget {
    private final ConfigSetting setting;
    private final ServerConfigEdits edits;
    private final ConfigRange range;

    private SettingSlider(ConfigSetting setting, ServerConfigEdits edits, ConfigRange range) {
      super(
          0,
          0,
          WIDTH,
          HEIGHT,
          ServerConfigText.value(edits.valueOf(setting)),
          range.fractionOf(Integer.parseInt(edits.valueOf(setting))));
      this.setting = setting;
      this.edits = edits;
      this.range = range;
    }

    @Override
    protected void updateMessage() {
      setMessage(ServerConfigText.value(Integer.toString(range.valueAt(value))));
    }

    @Override
    protected void applyValue() {
      edits.set(setting, Integer.toString(range.valueAt(value)));
    }
  }
}
