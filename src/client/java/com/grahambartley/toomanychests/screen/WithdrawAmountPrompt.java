package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class WithdrawAmountPrompt {
  private static final int PADDING = 8;
  private static final int LINE_HEIGHT = 9;
  private static final int FIELD_HEIGHT = 12;
  private static final int GAP = 4;

  private static final int MIN_WIDTH = 120;

  private static final float ELEVATION = 300.0F;

  private ItemVariant variant;
  private AmountEntry entry;

  public void open(ItemVariant asked, int ceiling) {
    variant = asked;
    entry = new AmountEntry(ceiling);
  }

  public void close() {
    variant = null;
    entry = null;
  }

  public boolean isOpen() {
    return variant != null;
  }

  public ItemVariant variant() {
    return variant;
  }

  public int amount() {
    return entry == null ? 0 : entry.value();
  }

  public int requested() {
    return entry == null ? 0 : entry.requested();
  }

  public boolean asksForMoreThanThereIs() {
    return entry != null && entry.asksForMoreThanThereIs();
  }

  public PromptKey keyPressed(int keyCode, boolean controlDown) {
    return isOpen() ? entry.keyPressed(keyCode, controlDown) : PromptKey.HANDLED;
  }

  public PromptKey charTyped(char typed) {
    return isOpen() ? entry.charTyped(typed) : PromptKey.HANDLED;
  }

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    final int width = Math.min(TerminalScreenLayout.GRID_WIDTH, MIN_WIDTH);
    final int inner = width - 2 * PADDING;
    final List<OrderedText> heading =
        textRenderer.wrapLines(
            TerminalScreenText.of("amount.title", variant.toStack().getName()), inner);
    final List<OrderedText> available =
        textRenderer.wrapLines(TerminalScreenText.of("amount.available", entry.ceiling()), inner);
    final List<OrderedText> hint =
        textRenderer.wrapLines(
            TerminalScreenText.of(entry.takesEverything() ? "amount.hint.all" : "amount.hint"),
            inner);
    final int height =
        2 * PADDING
            + 3 * GAP
            + FIELD_HEIGHT
            + LINE_HEIGHT * (heading.size() + available.size() + hint.size());

    final int left =
        originX + TerminalScreenLayout.GRID_X + (TerminalScreenLayout.GRID_WIDTH - width) / 2;
    final int top =
        originY + TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - height) / 2;

    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(context, left, top, width, height);
    int lineY = top + PADDING;
    lineY =
        drawLines(context, textRenderer, heading, left, lineY, width, VanillaPalette.LABEL) + GAP;
    lineY =
        drawLines(context, textRenderer, available, left, lineY, width, VanillaPalette.MUTED) + GAP;
    TerminalSprite.FIELD_FOCUSED.draw(context, left + PADDING, lineY, inner, FIELD_HEIGHT);
    drawLines(
        context,
        textRenderer,
        List.of(Text.literal(entry.text()).asOrderedText()),
        left,
        lineY + 2,
        width,
        VanillaPalette.FIELD_TEXT);
    drawLines(
        context, textRenderer, hint, left, lineY + FIELD_HEIGHT + GAP, width, VanillaPalette.MUTED);
    context.getMatrices().pop();
  }

  private static int drawLines(
      DrawContext context,
      TextRenderer textRenderer,
      List<OrderedText> lines,
      int left,
      int top,
      int width,
      int colour) {
    int lineY = top;
    for (final OrderedText line : new ArrayList<>(lines)) {
      context.drawText(
          textRenderer,
          line,
          left + (width - textRenderer.getWidth(line)) / 2,
          lineY,
          colour,
          false);
      lineY += LINE_HEIGHT;
    }
    return lineY;
  }
}
