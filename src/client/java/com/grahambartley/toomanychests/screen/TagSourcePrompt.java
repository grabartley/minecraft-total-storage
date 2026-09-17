package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.OwnedTerminal;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class TagSourcePrompt {
  private static final float ELEVATION = 300.0F;

  private final ListingScroll scroll = new ListingScroll(1, TagPromptLayout.VISIBLE_ROWS);

  private boolean open;
  private BlockPos armedForReplace;

  public void open() {
    open = true;
    armedForReplace = null;
    scroll.scrollTo(0);
  }

  public void close() {
    open = false;
    armedForReplace = null;
  }

  public boolean isArmed(OwnedTerminal source) {
    return source != null && source.pos().equals(armedForReplace);
  }

  public void armForReplace(OwnedTerminal source) {
    armedForReplace = source == null ? null : source.pos();
  }

  public void disarm() {
    armedForReplace = null;
  }

  public boolean isOpen() {
    return open;
  }

  public void fit(List<OwnedTerminal> sources) {
    scroll.fit(sources.size());
  }

  public boolean scrollBy(int notches) {
    return scroll.scrollBy(notches);
  }

  public OwnedTerminal sourceAt(List<OwnedTerminal> sources, int windowX, int windowY) {
    final int row = TagPromptLayout.rowAt(windowX, windowY);
    if (row == TagPromptLayout.NO_ROW) {
      return null;
    }
    final int index = scroll.firstIndex() + row;
    return index >= sources.size() ? null : sources.get(index);
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      List<OwnedTerminal> sources,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    if (!open) {
      return;
    }
    final int left = originX + TagPromptLayout.left();
    final int top = originY + TagPromptLayout.top();
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(context, left, top, TagPromptLayout.WIDTH, TagPromptLayout.HEIGHT);
    drawLine(
        context,
        textRenderer,
        TerminalScreenText.of("tags.copy.title"),
        left + TagPromptLayout.PADDING,
        top + TagPromptLayout.PADDING,
        VanillaPalette.LABEL);
    drawRows(context, textRenderer, sources, originX, originY, mouseX, mouseY);
    drawLine(
        context,
        textRenderer,
        sources.isEmpty()
            ? TerminalScreenText.of("tags.copy.none")
            : TerminalScreenText.of("tags.copy.hint"),
        left + TagPromptLayout.PADDING,
        top + TagPromptLayout.HEIGHT - TagPromptLayout.PADDING - TagPromptLayout.LINE_HEIGHT,
        VanillaPalette.MUTED);
    context.getMatrices().pop();
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      List<OwnedTerminal> sources,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final OwnedTerminal hovered = sourceAt(sources, mouseX, mouseY);
    for (int row = 0; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      if (index >= sources.size()) {
        return;
      }
      final OwnedTerminal source = sources.get(index);
      final int left = originX + TagPromptLayout.left() + TagPromptLayout.PADDING;
      final int top = originY + TagPromptLayout.rowY(row);
      final boolean armed = isArmed(source);
      TerminalChrome.drawActiveRow(
          context,
          armed,
          source.equals(hovered),
          left,
          top,
          TagPromptLayout.ROW_WIDTH,
          TagPromptLayout.ROW_HEIGHT);
      drawLine(
          context,
          textRenderer,
          armed ? TerminalScreenText.of("tags.copy.replace.confirm") : TagText.source(source),
          left + TagPromptLayout.TEXT_MARGIN,
          top + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.LINE_HEIGHT) / 2 + 1,
          armed
              ? VanillaPalette.WARNING
              : source.holdsAnythingToCopy() ? VanillaPalette.LABEL : VanillaPalette.MUTED);
    }
  }

  private static void drawLine(
      DrawContext context, TextRenderer textRenderer, Text text, int left, int top, int colour) {
    final String trimmed = TrimmedText.toWidth(textRenderer, text, TagPromptLayout.ROW_WIDTH);
    context.drawText(textRenderer, trimmed, left, top, colour, false);
  }
}
