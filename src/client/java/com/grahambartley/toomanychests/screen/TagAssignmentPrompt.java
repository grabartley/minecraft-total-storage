package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class TagAssignmentPrompt {
  private final TagPromptList list = new TagPromptList();

  private ItemVariant subject;

  public void open(ItemVariant asked) {
    subject = asked;
    list.reset();
  }

  public void close() {
    subject = null;
    list.releaseScrollbar();
  }

  public boolean isOpen() {
    return subject != null;
  }

  public ItemVariant subject() {
    return subject;
  }

  public TagPromptList list() {
    return list;
  }

  public void fit(NetworkTagView view) {
    list.fit(view.custom().size());
  }

  public boolean scrollBy(int notches) {
    return list.scrollBy(notches);
  }

  public NetworkTagEntry tagAt(NetworkTagView view, int windowX, int windowY) {
    final int index = list.indexAt(windowX, windowY);
    final List<NetworkTagEntry> tags = view.custom();
    return index < 0 || index >= tags.size() ? null : tags.get(index);
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int matching,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    if (!isOpen()) {
      return;
    }
    TagPromptFrame.draw(
        context,
        textRenderer,
        TerminalScreenText.of("tags.assign.title", subject.toStack().getName()),
        view.custom().isEmpty()
            ? TerminalScreenText.of("tags.assign.none")
            : TagText.bulkHint(matching),
        list,
        originX,
        originY,
        () -> drawRows(context, textRenderer, view, originX, originY, mouseX, mouseY));
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> all = view.custom();
    final NetworkTagEntry hovered = tagAt(view, mouseX, mouseY);
    for (int row = 0; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      final int index = list.indexOfRow(row);
      if (index >= all.size()) {
        return;
      }
      final NetworkTagEntry tag = all.get(index);
      TagCheckRow.draw(
          context,
          textRenderer,
          FilterColumnText.nameOf(tag),
          view.subjectCarries(tag.id()),
          tag.equals(hovered),
          originX + TagPromptLayout.textLeft(),
          originY + TagPromptLayout.rowY(row));
    }
  }
}
