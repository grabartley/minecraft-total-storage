package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class TerminalIconButton extends ClickableWidget {
  private final TerminalSprite icon;
  private final Runnable onPress;

  public TerminalIconButton(int x, int y, TerminalSprite icon, Text label, Runnable onPress) {
    super(x, y, TerminalScreenLayout.BUTTON_SIZE, TerminalScreenLayout.BUTTON_SIZE, label);
    this.icon = Objects.requireNonNull(icon, "icon");
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    setTooltip(Tooltip.of(label));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    TerminalChrome.controlOrUnavailable(active, isSelected())
        .draw(context, getX(), getY(), getWidth(), getHeight());
    icon.draw(
        context,
        getX() + TerminalScreenLayout.ICON_INSET,
        getY() + TerminalScreenLayout.ICON_INSET,
        TerminalScreenLayout.ICON_SIZE,
        TerminalScreenLayout.ICON_SIZE);
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
