package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ConfigSection;
import com.grahambartley.totalstorage.config.ConfigSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ElementListWidget;

public final class ConfigList extends ElementListWidget<ConfigEntry> {
  static final int ROW_HEIGHT = 26;
  private static final int WIDEST_ROW = 340;
  private static final int EDGE_MARGIN = 20;

  public ConfigList(MinecraftClient client, int width, int height, int top) {
    super(client, width, height, top, ROW_HEIGHT);
  }

  public void addPreferences(ClientPreferenceEdits edits) {
    final TextRenderer textRenderer = client.textRenderer;
    addEntry(new ConfigHeading(textRenderer, ClientPreferenceText.section()));
    for (final ClientPreference preference : ClientPreference.values()) {
      addEntry(
          new ConfigRow(
              textRenderer,
              ClientPreferenceText.label(preference),
              ClientPreferenceText.defaultLine(preference),
              ClientPreferenceControls.of(preference, edits)));
    }
  }

  public void addServerSettings(ServerConfigEdits edits, boolean editable) {
    final TextRenderer textRenderer = client.textRenderer;
    for (final ConfigSection section : ConfigSection.values()) {
      addEntry(new ConfigHeading(textRenderer, ServerConfigText.section(section)));
      for (final ConfigSetting setting : section.settings()) {
        addEntry(
            new ConfigRow(
                textRenderer,
                ServerConfigText.label(setting),
                ServerConfigText.bounds(setting),
                ServerConfigControls.of(setting, edits, editable)));
      }
    }
  }

  @Override
  public int getRowWidth() {
    return Math.min(WIDEST_ROW, getWidth() - EDGE_MARGIN);
  }
}
