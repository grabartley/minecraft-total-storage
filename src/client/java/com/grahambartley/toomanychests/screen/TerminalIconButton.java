package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A square toolbar button wearing the terminal's own chassis and one 12x12 glyph.
 *
 * <p>Not a vanilla {@code ButtonWidget}, because a vanilla button draws vanilla chrome and would
 * read as something bolted onto the screen rather than part of it. The three states come from the
 * sprite set rather than from a tint, so the resting, hovered and unavailable buttons differ by
 * shape and stay apart for players who cannot separate the accent from the chassis.
 */
public final class TerminalIconButton extends ClickableWidget {
  private static final Identifier NORMAL = TerminalSprites.of("button/normal");
  private static final Identifier HOVER = TerminalSprites.of("button/hover");
  private static final Identifier DISABLED = TerminalSprites.of("button/disabled");

  private final Identifier icon;
  private final Runnable onPress;

  public TerminalIconButton(int x, int y, Identifier icon, Text label, Runnable onPress) {
    super(x, y, TerminalScreenLayout.BUTTON_SIZE, TerminalScreenLayout.BUTTON_SIZE, label);
    this.icon = Objects.requireNonNull(icon, "icon");
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    setTooltip(Tooltip.of(label));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    context.drawGuiTexture(chromeFor(), getX(), getY(), getWidth(), getHeight());
    context.drawGuiTexture(
        icon,
        getX() + TerminalScreenLayout.ICON_INSET,
        getY() + TerminalScreenLayout.ICON_INSET,
        TerminalScreenLayout.ICON_SIZE,
        TerminalScreenLayout.ICON_SIZE);
  }

  private Identifier chromeFor() {
    if (!active) {
      return DISABLED;
    }
    return isSelected() ? HOVER : NORMAL;
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
