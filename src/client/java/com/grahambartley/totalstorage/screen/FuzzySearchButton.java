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

  private boolean on;

  public FuzzySearchButton(int originX, int originY, boolean on, Consumer<Boolean> sender) {
    super(
        originX + TerminalScreenLayout.FUZZY_X,
        originY + TerminalScreenLayout.FUZZY_Y,
        TerminalScreenLayout.FUZZY_SIZE,
        TerminalScreenLayout.FUZZY_SIZE,
        label(on));
    this.sender = Objects.requireNonNull(sender, "sender");
    show(on);
  }

  public static Text label(boolean on) {
    return TerminalScreenText.of(on ? "search.fuzzy.on" : "search.fuzzy.off");
  }

  public static Text tooltip(boolean on) {
    return Text.empty()
        .append(label(on))
        .append("\n")
        .append(TerminalScreenText.of("search.fuzzy.hint").formatted(Formatting.GRAY));
  }

  public static TerminalSprite icon(boolean on) {
    return on ? TerminalSprite.FUZZY : TerminalSprite.FUZZY_OFF;
  }

  public boolean isOn() {
    return on;
  }

  public void update(boolean shown, boolean wanted) {
    visible = shown;
    if (wanted != on) {
      show(wanted);
    }
  }

  private void show(boolean wanted) {
    on = wanted;
    setMessage(label(wanted));
    setTooltip(Tooltip.of(tooltip(wanted)));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    TerminalIconButton.draw(context, this, true, icon(on));
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    sender.accept(!on);
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    appendDefaultNarrations(builder);
  }
}
