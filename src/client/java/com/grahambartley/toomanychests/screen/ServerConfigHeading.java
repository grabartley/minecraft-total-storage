package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigSection;
import net.minecraft.client.font.TextRenderer;

public final class ServerConfigHeading extends ServerConfigEntry {
  public ServerConfigHeading(TextRenderer textRenderer, ConfigSection section) {
    super(textRenderer, ServerConfigText.section(section));
  }
}
