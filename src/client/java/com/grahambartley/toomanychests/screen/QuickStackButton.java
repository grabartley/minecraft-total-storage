package com.grahambartley.toomanychests.screen;

import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class QuickStackButton extends ClickableWidget {
  public static final int SIZE = 14;

  private final Runnable onPress;

  private boolean held;

  public QuickStackButton(int x, int y, Text label, Runnable onPress) {
    super(x, y, SIZE, SIZE, label);
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    setTooltip(Tooltip.of(label));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    QuickStackButtonSprite.of(active, isSelected(), held)
        .draw(context, getX(), getY(), getWidth(), getHeight());
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    held = true;
    onPress.run();
  }

  @Override
  public void onRelease(double mouseX, double mouseY) {
    held = false;
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    appendDefaultNarrations(builder);
  }
}
