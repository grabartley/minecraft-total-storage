package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ConfigOption;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public final class ConfigCycler {
  private ConfigCycler() {}

  public static <S> CyclingButtonWidget<String> of(
      ConfigOption<S> option,
      ConfigDraft<S> draft,
      List<String> choices,
      Function<String, Text> display,
      Text label) {
    return CyclingButtonWidget.<String>builder(display::apply)
        .values(choices)
        .initially(draft.valueOf(option))
        .omitKeyText()
        .build(
            0,
            0,
            ConfigRow.CONTROL_WIDTH,
            ConfigRow.CONTROL_HEIGHT,
            label,
            (button, chosen) -> draft.set(option, chosen));
  }
}
