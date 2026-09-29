package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.OwnedTerminal;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.BlockPos;

public final class TagSourcePrompt {
  private final TagPromptList list = new TagPromptList();

  private boolean open;
  private BlockPos armedForReplace;

  public void open() {
    open = true;
    armedForReplace = null;
    list.reset();
  }

  public void close() {
    open = false;
    armedForReplace = null;
    list.releaseScrollbar();
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

  public TagPromptList list() {
    return list;
  }

  public void fit(List<OwnedTerminal> sources) {
    list.fit(sources.size());
  }

  public boolean scrollBy(int notches) {
    return list.scrollBy(notches);
  }

  public OwnedTerminal sourceAt(List<OwnedTerminal> sources, int windowX, int windowY) {
    final int index = list.indexAt(windowX, windowY);
    return index < 0 || index >= sources.size() ? null : sources.get(index);
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
    TagPromptFrame.draw(
        context,
        textRenderer,
        TerminalScreenText.of("tags.copy.title"),
        sources.isEmpty()
            ? TerminalScreenText.of("tags.copy.none")
            : TerminalScreenText.of("tags.copy.hint"),
        list,
        originX,
        originY,
        () -> drawRows(context, textRenderer, sources, originX, originY, mouseX, mouseY));
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
      final int index = list.indexOfRow(row);
      if (index >= sources.size()) {
        return;
      }
      final OwnedTerminal source = sources.get(index);
      final int left = originX + TagPromptLayout.textLeft();
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
      final OrderedText text =
          TrimmedText.toWidth(
              textRenderer,
              armed ? TerminalScreenText.of("tags.copy.replace.confirm") : TagText.source(source),
              TagPromptLayout.ROW_WIDTH - 2 * TagPromptLayout.TEXT_MARGIN);
      context.drawText(
          textRenderer,
          text,
          left + TagPromptLayout.TEXT_MARGIN,
          top + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.LINE_HEIGHT) / 2 + 1,
          armed
              ? VanillaPalette.WARNING
              : source.holdsAnythingToCopy() ? VanillaPalette.LABEL : VanillaPalette.MUTED,
          false);
    }
  }
}
