package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientPreference;
import net.minecraft.client.gui.widget.CyclingButtonWidget;

public final class ClientPreferenceControls {
  private ClientPreferenceControls() {}

  public static CyclingButtonWidget<String> of(
      ClientPreference preference, ClientPreferenceEdits edits) {
    return CyclingButtonWidget.<String>builder(
            value -> ClientPreferenceText.displayValue(preference, value))
        .values(preference.choices())
        .initially(edits.valueOf(preference))
        .omitKeyText()
        .build(
            0,
            0,
            ServerConfigControls.WIDTH,
            ServerConfigControls.HEIGHT,
            ClientPreferenceText.label(preference),
            (button, chosen) -> edits.set(preference, chosen));
  }
}
