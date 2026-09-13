package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigSetting;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class ServerConfigRow extends ServerConfigEntry {
  private static final int BOUNDS_TOP = 14;
  private static final int BOUNDS_COLOUR = 0xFFA0A0A0;

  private final Text label;
  private final Text bounds;
  private final ClickableWidget control;

  public ServerConfigRow(
      TextRenderer textRenderer, ConfigSetting setting, ServerConfigEdits edits, boolean editable) {
    super(textRenderer);
    this.label = ServerConfigText.label(setting);
    this.bounds = ServerConfigText.bounds(setting);
    this.control = ServerConfigControls.of(setting, edits);
    this.control.active = editable;
  }

  @Override
  public void render(
      DrawContext context,
      int index,
      int y,
      int x,
      int entryWidth,
      int entryHeight,
      int mouseX,
      int mouseY,
      boolean hovered,
      float delta) {
    drawLabel(context, label, x, y + LABEL_TOP, LABEL_COLOUR);
    drawLabel(context, bounds, x, y + BOUNDS_TOP, BOUNDS_COLOUR);
    control.setPosition(x + entryWidth - ServerConfigControls.WIDTH, y);
    control.render(context, mouseX, mouseY, delta);
  }

  @Override
  public List<? extends Element> children() {
    return List.of(control);
  }

  @Override
  public List<? extends Selectable> selectableChildren() {
    return List.of(control);
  }
}
