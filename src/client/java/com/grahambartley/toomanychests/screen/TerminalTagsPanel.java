package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TagRowRegion;
import com.grahambartley.toomanychests.screenhandler.TagsSectionLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TerminalTagsPanel {
  private static final Identifier ROW = TerminalSprites.of("filter/row");
  private static final Identifier ROW_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier ROW_ACTIVE = TerminalSprites.of("filter/row_active");
  private static final Identifier CONTROL = TerminalSprites.of("button/normal");
  private static final Identifier CONTROL_HOVER = TerminalSprites.of("button/hover");
  private static final Identifier CONTROL_DISABLED = TerminalSprites.of("button/disabled");
  private static final Identifier CLEAR_ICON = TerminalSprites.of("icon/clear_all");

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int MUTED_COLOUR = 0x9AA3B3;
  private static final int WARNING_COLOUR = 0xED6353;
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

  public NetworkTagEntry tagAt(NetworkTagView view, int firstVisible, int windowX, int windowY) {
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
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    drawNewTagRow(context, textRenderer, view, originX, originY, mouseX, mouseY);
    drawTagRows(context, textRenderer, view, firstVisible, originX, originY, mouseX, mouseY);
  }

  private static void drawNewTagRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int top = originY + TerminalSettingsLayout.rowY(TagsSectionLayout.NEW_TAG_ROW);
    final boolean hovered = view.mayAdminister() && TagsSectionLayout.isOverNewTag(mouseX, mouseY);
    context.drawGuiTexture(
        hovered ? ROW_HOVER : ROW,
        left,
        top,
        TerminalSettingsLayout.ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT);
    final Text label =
        view.mayAdminister()
            ? TerminalScreenText.of("tags.new")
            : TerminalScreenText.of("tags.read_only");
    context.drawText(
        textRenderer,
        label,
        left + TagsSectionLayout.TEXT_MARGIN,
        top + (TerminalSettingsLayout.ROW_HEIGHT - LINE_HEIGHT) / 2,
        view.mayAdminister() ? LABEL_COLOUR : MUTED_COLOUR,
        false);
  }

  private void drawTagRows(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int firstVisible,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> custom = view.custom();
    final NetworkTagEntry hovered = tagAt(view, firstVisible, mouseX, mouseY);
    for (int row = 0; row < TagsSectionLayout.TAG_ROWS; row++) {
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
    context.drawGuiTexture(
        armed ? ROW_ACTIVE : hovered ? ROW_HOVER : ROW,
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
        armed ? WARNING_COLOUR : LABEL_COLOUR,
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
    final int left = originX + TagsSectionLayout.DELETE_X;
    final int top = originY + TagsSectionLayout.deleteY(row);
    final boolean hovered =
        view.mayAdminister()
            && TagsSectionLayout.regionAt(mouseX, mouseY) == TagRowRegion.DELETE
            && TerminalSettingsLayout.rowAt(mouseX, mouseY) == row;
    context.drawGuiTexture(
        !view.mayAdminister() ? CONTROL_DISABLED : hovered ? CONTROL_HOVER : CONTROL,
        left,
        top,
        TagsSectionLayout.CONTROL_SIZE,
        TagsSectionLayout.CONTROL_SIZE);
    final int inset = (TagsSectionLayout.CONTROL_SIZE - ICON_SIZE) / 2;
    context.drawGuiTexture(CLEAR_ICON, left + inset, top + inset, ICON_SIZE, ICON_SIZE);
  }
}
