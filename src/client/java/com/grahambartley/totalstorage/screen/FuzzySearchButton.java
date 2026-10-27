package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class FuzzySearchButton extends ClickableWidget {
  private final Consumer<Boolean> sender;

  private boolean enabled;

  public FuzzySearchButton(int originX, int originY, boolean enabled, Consumer<Boolean> sender) {
    super(
        originX + TerminalScreenLayout.FUZZY_X,
        originY + TerminalScreenLayout.FUZZY_Y,
        TerminalScreenLayout.FUZZY_SIZE,
        TerminalScreenLayout.FUZZY_SIZE,
        label(enabled));
    this.sender = Objects.requireNonNull(sender, "sender");
    show(enabled);
  }

  public static Text label(boolean enabled) {
    return TerminalScreenText.of(enabled ? "search.fuzzy.on" : "search.fuzzy.off");
  }

  public static Text tooltip(boolean enabled) {
    return Text.empty()
        .append(label(enabled))
        .append("\n")
        .append(TerminalScreenText.of("search.fuzzy.hint").formatted(Formatting.GRAY));
  }

  public static TerminalSprite icon(boolean enabled) {
    return enabled ? TerminalSprite.FUZZY : TerminalSprite.FUZZY_OFF;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void update(boolean shown, boolean wanted) {
    visible = shown;
    if (wanted != enabled) {
      show(wanted);
    }
  }

  private void show(boolean wanted) {
    enabled = wanted;
    setMessage(label(wanted));
    setTooltip(Tooltip.of(tooltip(wanted)));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    VanillaSprite.button(true, isSelected()).draw(context, getX(), getY(), getWidth(), getHeight());
    final int offset = TerminalIconButton.iconOffset(getWidth());
    icon(enabled)
        .draw(
            context,
            getX() + offset,
            getY() + offset - 1,
            TerminalIconButton.ICON_SIZE,
            TerminalIconButton.ICON_SIZE);
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    sender.accept(!enabled);
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    appendDefaultNarrations(builder);
  }
}
