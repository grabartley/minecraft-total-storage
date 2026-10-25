package com.grahambartley.totalstorage.screen;

import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TerminalTextButton extends ClickableWidget {
  public static final int TEXT_MARGIN = 2;

  private static final int LINE_HEIGHT = 8;

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

  public static int faceInset() {
    return 2;
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    final boolean hovered = isSelected();
    VanillaSprite.button(active, hovered).draw(context, getX(), getY(), getWidth(), getHeight());
    if (accent && active) {
      TerminalChrome.fill(
          context,
          getX() + faceInset(),
          getY() + faceInset(),
          getWidth() - 2 * faceInset(),
          getHeight() - 2 * faceInset() - 1,
          VanillaPalette.ACCENT);
    }
    final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    final int room = getWidth() - 2 * TEXT_MARGIN;
    final OrderedText label = TrimmedText.toWidth(textRenderer, getMessage(), room);
    context.drawText(
        textRenderer,
        label,
        getX() + (getWidth() - textRenderer.getWidth(label) + 1) / 2,
        getY() + (getHeight() - LINE_HEIGHT) / 2,
        VanillaPalette.opaque(
            active ? VanillaPalette.BUTTON_TEXT : VanillaPalette.BUTTON_TEXT_DISABLED),
        true);
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
