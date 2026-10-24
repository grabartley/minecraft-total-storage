package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TagRowRegion;
import com.grahambartley.totalstorage.screenhandler.TagsSectionLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TerminalTagsPanel {
  private static final int LINE_HEIGHT = 9;
  private static final int ICON_SIZE = 8;

  private String armedForDelete = "";

  public void disarm() {
    armedForDelete = "";
  }

  public boolean isArmed(String tagId) {
    return !armedForDelete.isEmpty() && armedForDelete.equals(tagId);
  }

  public void arm(String tagId) {
    armedForDelete = tagId == null ? "" : tagId;
  }

  public NetworkTagEntry tagAt(
      NetworkTagView view, int firstVisible, int hiddenRows, int windowX, int windowY) {
    if (TerminalSettingsLayout.rowAt(windowX, windowY) < hiddenRows) {
      return null;
    }
    final int index = TagsSectionLayout.tagIndexAt(windowX, windowY, firstVisible);
    final List<NetworkTagEntry> custom = view.custom();
    return index == TerminalSettingsLayout.NO_ROW || index >= custom.size()
        ? null
        : custom.get(index);
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int firstVisible,
      int hiddenRows,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    if (hiddenRows <= TagsSectionLayout.NEW_TAG_ROW) {
      drawNewTagRow(context, textRenderer, view, originX, originY, mouseX, mouseY);
    }
    drawTagRows(
        context, textRenderer, view, firstVisible, hiddenRows, originX, originY, mouseX, mouseY);
  }

  private void drawNewTagRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    drawFixedRow(
        context,
        textRenderer,
        view,
        TagsSectionLayout.NEW_TAG_ROW,
        TagsSectionLayout.isOverNewTag(mouseX, mouseY)
            && !TagsSectionLayout.isOverCopy(mouseX, mouseY),
        view.mayAdminister()
            ? TerminalScreenText.of("tags.new")
            : TerminalScreenText.of("tags.read_only"),
        originX,
        originY);
    drawControl(
        context,
        view,
        TerminalSprite.COPY_TAGS,
        TagsSectionLayout.NEW_TAG_ROW,
        TagsSectionLayout.isOverCopy(mouseX, mouseY),
        originX,
        originY);
  }

  private static void drawFixedRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int row,
      boolean over,
      Text label,
      int originX,
      int originY) {
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int top = originY + TerminalSettingsLayout.rowY(row);
    TerminalChrome.drawRow(
        context,
        view.mayAdminister() && over,
        left,
        top,
        TerminalSettingsLayout.ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT);
    context.drawText(
        textRenderer,
        label,
        left + TagsSectionLayout.TEXT_MARGIN,
        top + (TerminalSettingsLayout.ROW_HEIGHT - LINE_HEIGHT) / 2,
        view.mayAdminister() ? VanillaPalette.LABEL : VanillaPalette.MUTED,
        false);
  }

  private void drawTagRows(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int firstVisible,
      int hiddenRows,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> custom = view.custom();
    final NetworkTagEntry hovered = tagAt(view, firstVisible, hiddenRows, mouseX, mouseY);
    final int firstDrawn = Math.max(0, hiddenRows - TagsSectionLayout.FIRST_TAG_ROW);
    for (int row = firstDrawn; row < TagsSectionLayout.TAG_ROWS; row++) {
      final int index = firstVisible + row;
      if (index >= custom.size()) {
        return;
      }
      drawTagRow(
          context,
          textRenderer,
          view,
          custom.get(index),
          custom.get(index).equals(hovered),
          originX,
          originY,
          row + TagsSectionLayout.FIRST_TAG_ROW,
          mouseX,
          mouseY);
    }
  }

  private void drawTagRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      NetworkTagEntry tag,
      boolean hovered,
      int originX,
      int originY,
      int row,
      int mouseX,
      int mouseY) {
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int top = originY + TerminalSettingsLayout.rowY(row);
    final boolean armed = isArmed(tag.id());
    TerminalChrome.drawActiveRow(
        context,
        armed,
        hovered,
        left,
        top,
        TerminalSettingsLayout.ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT);
    final Text label = armed ? TerminalScreenText.of("tags.delete.confirm") : TagText.row(tag);
    final OrderedText name = TrimmedText.toWidth(textRenderer, label, TagsSectionLayout.NAME_WIDTH);
    context.drawText(
        textRenderer,
        name,
        left + TagsSectionLayout.TEXT_MARGIN,
        top + (TerminalSettingsLayout.ROW_HEIGHT - LINE_HEIGHT) / 2,
        armed ? VanillaPalette.WARNING : VanillaPalette.LABEL,
        false);
    drawDeleteControl(context, view, originX, originY, row, mouseX, mouseY);
  }

  private static void drawDeleteControl(
      DrawContext context,
      NetworkTagView view,
      int originX,
      int originY,
      int row,
      int mouseX,
      int mouseY) {
    final boolean hovered =
        TagsSectionLayout.regionAt(mouseX, mouseY) == TagRowRegion.DELETE
            && TerminalSettingsLayout.rowAt(mouseX, mouseY) == row;
    drawControl(context, view, TerminalSprite.CLEAR_ALL, row, hovered, originX, originY);
  }

  private static void drawControl(
      DrawContext context,
      NetworkTagView view,
      TerminalSprite icon,
      int row,
      boolean hovered,
      int originX,
      int originY) {
    final int left = originX + TagsSectionLayout.DELETE_X;
    final int top = originY + TagsSectionLayout.deleteY(row);
    TerminalChrome.controlOrUnavailable(view.mayAdminister(), view.mayAdminister() && hovered)
        .draw(context, left, top, TagsSectionLayout.CONTROL_SIZE, TagsSectionLayout.CONTROL_SIZE);
    final int inset = (TagsSectionLayout.CONTROL_SIZE - ICON_SIZE) / 2;
    icon.draw(context, left + inset, top + inset, ICON_SIZE, ICON_SIZE);
  }
}
