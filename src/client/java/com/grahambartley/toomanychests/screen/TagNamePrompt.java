package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TagNamePrompt {
  private static final int WIDTH = 140;
  private static final int PADDING = 8;
  private static final int LINE_HEIGHT = 9;
  private static final int FIELD_HEIGHT = 12;
  private static final int GAP = 4;

  private static final float ELEVATION = 300.0F;

  private TagNameEntry entry;
  private String renaming = "";

  public void openForNewTag() {
    entry = new TagNameEntry("");
    renaming = "";
  }

  public void openForRename(String tagId, String currentName) {
    entry = new TagNameEntry(currentName);
    renaming = tagId == null ? "" : tagId;
  }

  public void close() {
    entry = null;
    renaming = "";
  }

  public boolean isOpen() {
    return entry != null;
  }

  public boolean isRenaming() {
    return !renaming.isEmpty();
  }

  public String renaming() {
    return renaming;
  }

  public String name() {
    return entry == null ? "" : entry.text();
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
    final int inner = WIDTH - 2 * PADDING;
    final List<OrderedText> heading =
        textRenderer.wrapLines(
            TerminalScreenText.of(isRenaming() ? "tags.rename.title" : "tags.create.title"), inner);
    final List<OrderedText> hint =
        textRenderer.wrapLines(TerminalScreenText.of("tags.name.hint"), inner);
    final int height =
        2 * PADDING + 2 * GAP + FIELD_HEIGHT + LINE_HEIGHT * (heading.size() + hint.size());
    final int left =
        originX + TerminalScreenLayout.GRID_X + (TerminalScreenLayout.GRID_WIDTH - WIDTH) / 2;
    final int top =
        originY + TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - height) / 2;

    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(context, left, top, WIDTH, height);
    int lineY = top + PADDING;
    lineY = drawLines(context, textRenderer, heading, left, lineY, VanillaPalette.LABEL) + GAP;
    TerminalSprite.FIELD_FOCUSED.draw(context, left + PADDING, lineY, inner, FIELD_HEIGHT);
    drawLines(
        context,
        textRenderer,
        List.of(Text.literal(entry.text()).asOrderedText()),
        left,
        lineY + 2,
        VanillaPalette.FIELD_TEXT);
    drawLines(context, textRenderer, hint, left, lineY + FIELD_HEIGHT + GAP, VanillaPalette.MUTED);
    context.getMatrices().pop();
  }

  private static int drawLines(
      DrawContext context,
      TextRenderer textRenderer,
      List<OrderedText> lines,
      int left,
      int top,
      int colour) {
    int lineY = top;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          left + (WIDTH - textRenderer.getWidth(line)) / 2,
          lineY,
          colour,
          false);
      lineY += LINE_HEIGHT;
    }
    return lineY;
  }
}
