package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ClientPreferences;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClientPreferenceControlsTest {
  private static final ClientPreferences SAVED =
      new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, true);

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void theControlOpensOnThePlayersSavedValue(ClientPreference preference) {
    final CyclingButtonWidget<String> control =
        ClientPreferenceControls.of(preference, new ClientPreferenceEdits(SAVED));

    assertEquals(preference.valueIn(SAVED), control.getValue());
    assertEquals(
        ClientPreferenceText.displayValue(preference, preference.valueIn(SAVED)).getString(),
        control.getMessage().getString());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void theControlMatchesTheServerSettingsControls(ClientPreference preference) {
    final CyclingButtonWidget<String> control =
        ClientPreferenceControls.of(preference, new ClientPreferenceEdits(SAVED));

    assertEquals(ServerConfigControls.WIDTH, control.getWidth());
    assertEquals(ServerConfigControls.HEIGHT, control.getHeight());
    assertTrue(control.active);
  }
}
