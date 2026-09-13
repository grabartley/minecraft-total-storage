package com.grahambartley.toomanychests.screen;

import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class ScreenIconButton extends ClickableWidget {
  private final IconButtonSprites sprites;
  private final Runnable onPress;

  private boolean held;

  public ScreenIconButton(IconButtonSprites sprites, Text label, Runnable onPress) {
    super(0, 0, ScreenButtonRow.SIZE, ScreenButtonRow.SIZE, label);
    this.sprites = Objects.requireNonNull(sprites, "sprites");
    this.onPress = Objects.requireNonNull(onPress, "onPress");
    setTooltip(Tooltip.of(label));
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    sprites
        .select(active, isSelected(), held)
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
