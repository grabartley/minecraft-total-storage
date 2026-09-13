package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigSection;
import com.grahambartley.toomanychests.config.ConfigSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

public final class ServerConfigList extends ElementListWidget<ServerConfigEntry> {
  private static final int ROW_HEIGHT = 26;
  private static final int ROW_WIDTH = 310;

  public ServerConfigList(
      MinecraftClient client,
      int width,
      int height,
      int top,
      ServerConfigEdits edits,
      boolean editable) {
    super(client, width, height, top, ROW_HEIGHT);
    for (final ConfigSection section : ConfigSection.values()) {
      addEntry(new ServerConfigHeading(client.textRenderer, section));
      for (final ConfigSetting setting : section.settings()) {
        addEntry(new ServerConfigRow(client.textRenderer, setting, edits, editable));
      }
    }
  }

  @Override
  public int getRowWidth() {
    return ROW_WIDTH;
  }
}
