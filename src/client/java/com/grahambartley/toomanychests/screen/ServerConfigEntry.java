package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigSetting;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ServerConfigEntry extends ElementListWidget.Entry<ServerConfigEntry> {
  private static final int LABEL_TOP = 3;
  private static final int BOUNDS_TOP = 14;
  private static final int BOUNDS_COLOUR = 0xFFA0A0A0;
  private static final int LABEL_COLOUR = 0xFFFFFFFF;

  private final TextRenderer textRenderer;
  private final Text label;
  private final Text bounds;
  @Nullable private final ClickableWidget control;

  public ServerConfigEntry(
      TextRenderer textRenderer, ConfigSetting setting, ServerConfigEdits edits, boolean editable) {
    this.textRenderer = textRenderer;
    this.label = ServerConfigText.label(setting);
    this.bounds = ServerConfigText.bounds(setting);
    this.control = ServerConfigControls.of(setting, edits);
    this.control.active = editable;
  }

  protected ServerConfigEntry(TextRenderer textRenderer, Text heading) {
    this.textRenderer = textRenderer;
    this.label = heading;
    this.bounds = Text.empty();
    this.control = null;
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
    context.drawTextWithShadow(textRenderer, label, x, y + LABEL_TOP, LABEL_COLOUR);
    if (control == null) {
      return;
    }
    context.drawTextWithShadow(textRenderer, bounds, x, y + BOUNDS_TOP, BOUNDS_COLOUR);
    control.setPosition(x + entryWidth - ServerConfigControls.WIDTH, y);
    control.render(context, mouseX, mouseY, delta);
  }

  @Override
  public List<? extends Element> children() {
    return control == null ? List.of() : List.of(control);
  }

  @Override
  public List<? extends Selectable> selectableChildren() {
    return control == null ? List.of() : List.of(control);
  }
}
