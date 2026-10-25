package com.grahambartley.totalstorage.screen;

import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class TerminalTextButton extends ClickableWidget {
  private final Runnable onPress;
  private final Runnable onSecondaryPress;

  private boolean accent;
  private Text hint;
  private Text tooltipShown;

  public TerminalTextButton(int x, int y, int width, int height, Text label, Runnable onPress) {
    this(x, y, width, height, label, onPress, null);
  }

  public TerminalTextButton(
      int x,
      int y,
      int width,
      int height,
      Text label,
      Runnable onPress,
      Runnable onSecondaryPress) {
    super(x, y, width, height, label);
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    this.onSecondaryPress = onSecondaryPress;
  }

  public void show(Text label, boolean shownAsAccent) {
    if (!label.equals(getMessage())) {
      setMessage(label);
    }
    accent = shownAsAccent;
  }

  public void setHint(Text shown) {
    hint = shown;
  }

  public boolean isAccent() {
    return accent;
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    ButtonFace.draw(
        context,
        textRenderer,
        getMessage(),
        getX(),
        getY(),
        getWidth(),
        getHeight(),
        active,
        isSelected(),
        accent);
    final int room = ButtonFace.labelRoom(getWidth());
    showTooltip(
        hint != null ? hint : textRenderer.getWidth(getMessage()) > room ? getMessage() : null);
  }

  private void showTooltip(Text shown) {
    if (Objects.equals(tooltipShown, shown)) {
      return;
    }
    tooltipShown = shown;
    setTooltip(shown == null ? null : Tooltip.of(shown));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!active || !visible || !isMouseOver(mouseX, mouseY)) {
      return false;
    }
    if (button == GridClick.LEFT_BUTTON) {
      playDownSound(MinecraftClient.getInstance().getSoundManager());
      onPress.run();
      return true;
    }
    if (button == GridClick.RIGHT_BUTTON && onSecondaryPress != null) {
      playDownSound(MinecraftClient.getInstance().getSoundManager());
      onSecondaryPress.run();
      return true;
    }
    return false;
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
