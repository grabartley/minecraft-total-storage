package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.terminal.OwnedTerminal;
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
        null,
        TerminalScreenText.of("tags.copy.title"),
        list,
        sources.isEmpty()
            ? HintLine.of(HintLine.key("esc"), HintLine.words("hint.close"))
            : HintLine.of(
                HintLine.key("click"),
                HintLine.words("hint.merge"),
                HintLine.key("shift"),
                HintLine.words("hint.replace")),
        null,
        sources.isEmpty() ? TerminalScreenText.of("tags.copy.none") : null,
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
    final PromptListGeometry geometry = TagPromptLayout.LIST;
    for (int row = 0; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      final int index = list.indexOfRow(row);
      if (index >= sources.size()) {
        return;
      }
      final OwnedTerminal source = sources.get(index);
      final int left = originX + geometry.left();
      final int top = originY + geometry.rowY(row);
      final boolean armed = isArmed(source);
      if (armed) {
        VanillaSprite.button(true, source.equals(hovered))
            .draw(context, left, top, geometry.rowWidth(), geometry.rowHeight());
        TerminalChrome.fill(
            context,
            left + TerminalTextButton.faceInset(),
            top + TerminalTextButton.faceInset(),
            geometry.rowWidth() - 2 * TerminalTextButton.faceInset(),
            geometry.rowHeight() - 2 * TerminalTextButton.faceInset() - 1,
            VanillaPalette.ACCENT);
      } else if (source.equals(hovered)) {
        TerminalChrome.drawHover(context, left, top, geometry.rowWidth(), geometry.rowHeight());
      }
      final OrderedText text =
          TrimmedText.toWidth(
              textRenderer,
              armed ? TerminalScreenText.of("tags.copy.replace.armed") : TagText.source(source),
              geometry.rowWidth() - 2 * TagPromptLayout.TEXT_MARGIN);
      context.drawText(
          textRenderer,
          text,
          armed
              ? left + (geometry.rowWidth() - textRenderer.getWidth(text) + 1) / 2
              : left + TagPromptLayout.TEXT_MARGIN,
          top + (geometry.rowHeight() - 8) / 2,
          VanillaPalette.opaque(
              armed
                  ? VanillaPalette.BUTTON_TEXT
                  : source.holdsAnythingToCopy() ? VanillaPalette.LABEL : VanillaPalette.MUTED),
          armed);
    }
  }
}
