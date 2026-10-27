package com.grahambartley.totalstorage.screen;

import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class TerminalIconButton extends ClickableWidget {
  public static final int ICON_SIZE = 8;

  private final TerminalSprite icon;
  private final TerminalSprite unavailableIcon;
  private final Runnable onPress;

  public TerminalIconButton(
      int x,
      int y,
      int size,
      TerminalSprite icon,
      TerminalSprite unavailableIcon,
      Text label,
      Runnable onPress) {
    super(x, y, size, size, label);
    this.icon = Objects.requireNonNull(icon, "icon");
    this.unavailableIcon = Objects.requireNonNull(unavailableIcon, "unavailableIcon");
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    setTooltip(Tooltip.of(label));
  }

  public static int iconOffset(int size) {
    return (size - ICON_SIZE) / 2;
  }

  public static void draw(
      DrawContext context, ClickableWidget widget, boolean active, TerminalSprite icon) {
    VanillaSprite.button(active, widget.isSelected())
        .draw(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
    final int offset = iconOffset(widget.getWidth());
    icon.draw(context, widget.getX() + offset, widget.getY() + offset - 1, ICON_SIZE, ICON_SIZE);
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    draw(context, this, active, active ? icon : unavailableIcon);
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    onPress.run();
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    appendDefaultNarrations(builder);
  }
}
