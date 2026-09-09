package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TagAssignmentPrompt {
  private static final Identifier PANEL = TerminalSprites.of("panel/inset");
  private static final Identifier ROW = TerminalSprites.of("filter/row");
  private static final Identifier ROW_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier ROW_ACTIVE = TerminalSprites.of("filter/row_active");
  private static final Identifier CHECKED = TerminalSprites.of("slot/cell_selected");
  private static final Identifier UNCHECKED = TerminalSprites.of("slot/cell");

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int HINT_COLOUR = 0x9AA3B3;
  private static final int PRESET_COLOUR = 0x6D7484;

  private static final float ELEVATION = 300.0F;

  private final ListingScroll scroll = new ListingScroll(1, TagPromptLayout.VISIBLE_ROWS);

  private ItemVariant subject;

  public void open(ItemVariant asked) {
    subject = asked;
    scroll.scrollTo(0);
  }

  public void close() {
    subject = null;
  }

  public boolean isOpen() {
    return subject != null;
  }

  public ItemVariant subject() {
    return subject;
  }

  public void fit(NetworkTagView view) {
    scroll.fit(view.editableFirst().size());
  }

  public boolean scrollBy(int notches) {
    return scroll.scrollBy(notches);
  }

  public NetworkTagEntry tagAt(NetworkTagView view, int windowX, int windowY) {
    final int row = TagPromptLayout.rowAt(windowX, windowY);
    if (row == TagPromptLayout.NO_ROW) {
      return null;
    }
    final List<NetworkTagEntry> tags = view.editableFirst();
    final int index = scroll.firstIndex() + row;
    return index >= tags.size() ? null : tags.get(index);
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
    final int left = originX + TagPromptLayout.left();
    final int top = originY + TagPromptLayout.top();
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    context.drawGuiTexture(PANEL, left, top, TagPromptLayout.WIDTH, TagPromptLayout.HEIGHT);
    drawHeading(context, textRenderer, left, top);
    drawRows(context, textRenderer, view, originX, originY, mouseX, mouseY);
    drawFooter(context, textRenderer, view, matching, left, top);
    context.getMatrices().pop();
  }

  private void drawHeading(DrawContext context, TextRenderer textRenderer, int left, int top) {
    final Text heading = TerminalScreenText.of("tags.assign.title", subject.toStack().getName());
    final OrderedText trimmed =
        TrimmedText.toWidth(textRenderer, heading, TagPromptLayout.ROW_WIDTH);
    context.drawText(
        textRenderer,
        trimmed,
        left + TagPromptLayout.PADDING,
        top + TagPromptLayout.PADDING,
        LABEL_COLOUR,
        false);
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> all = view.editableFirst();
    final NetworkTagEntry hovered = tagAt(view, mouseX, mouseY);
    for (int row = 0; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      if (index >= all.size()) {
        return;
      }
      final NetworkTagEntry tag = all.get(index);
      drawRow(
          context,
          textRenderer,
          tag,
          view.subjectCarries(tag.id()),
          tag.equals(hovered) && !tag.immutable(),
          originX + TagPromptLayout.left() + TagPromptLayout.PADDING,
          originY + TagPromptLayout.rowY(row));
    }
  }

  private static void drawRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagEntry tag,
      boolean carried,
      boolean hovered,
      int left,
      int top) {
    context.drawGuiTexture(
        carried ? ROW_ACTIVE : hovered ? ROW_HOVER : ROW,
        left,
        top,
        TagPromptLayout.ROW_WIDTH,
        TagPromptLayout.ROW_HEIGHT);
    final int checkInset = (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.CHECK_SIZE) / 2;
    context.drawGuiTexture(
        carried ? CHECKED : UNCHECKED,
        left + checkInset,
        top + checkInset,
        TagPromptLayout.CHECK_SIZE,
        TagPromptLayout.CHECK_SIZE);
    final int textLeft =
        left + checkInset + TagPromptLayout.CHECK_SIZE + TagPromptLayout.TEXT_MARGIN;
    final OrderedText name =
        TrimmedText.toWidth(
            textRenderer,
            FilterColumnText.nameOf(tag),
            left + TagPromptLayout.ROW_WIDTH - textLeft);
    context.drawText(
        textRenderer,
        name,
        textLeft,
        top + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.LINE_HEIGHT) / 2 + 1,
        tag.immutable() ? PRESET_COLOUR : carried ? LABEL_COLOUR : HINT_COLOUR,
        false);
  }

  private static void drawFooter(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int matching,
      int left,
      int top) {
    final Text footer =
        view.custom().isEmpty()
            ? TerminalScreenText.of("tags.assign.none")
            : TerminalScreenText.of("tags.assign.bulk", matching);
    final OrderedText trimmed =
        TrimmedText.toWidth(textRenderer, footer, TagPromptLayout.ROW_WIDTH);
    context.drawText(
        textRenderer,
        trimmed,
        left + TagPromptLayout.PADDING,
        top + TagPromptLayout.HEIGHT - TagPromptLayout.PADDING - TagPromptLayout.LINE_HEIGHT,
        HINT_COLOUR,
        false);
  }
}
