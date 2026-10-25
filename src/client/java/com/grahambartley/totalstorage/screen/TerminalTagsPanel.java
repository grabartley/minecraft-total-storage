package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TagRowRegion;
import com.grahambartley.totalstorage.screenhandler.TagsSectionLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import com.grahambartley.totalstorage.text.CountedNoun;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TerminalTagsPanel {
  private static final int LINE_HEIGHT = 9;

  private static final int TEXT_Y = (TagsSectionLayout.ROW_HEIGHT - 8) / 2;

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

  public TagRowRegion regionAt(NetworkTagView view, int firstVisible, int windowX, int windowY) {
    final NetworkTagEntry tag = tagAt(view, firstVisible, windowX, windowY);
    if (tag == null) {
      return TagRowRegion.NONE;
    }
    return TagsSectionLayout.regionAt(windowX, windowY, isArmed(tag.id()));
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      int firstVisible,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> custom = view.custom();
    if (custom.isEmpty()) {
      drawEmpty(context, textRenderer, view);
    }
    final NetworkTagEntry hovered = tagAt(view, firstVisible, mouseX, mouseY);
    final TagRowRegion region = regionAt(view, firstVisible, mouseX, mouseY);
    for (int row = 0; row < TagsSectionLayout.TAG_ROWS; row++) {
      final int index = firstVisible + row;
      if (index >= custom.size()) {
        break;
      }
      final NetworkTagEntry tag = custom.get(index);
      drawRow(
          context,
          textRenderer,
          view.mayAdminister(),
          tag,
          tag.equals(hovered),
          tag.equals(hovered) ? region : TagRowRegion.NONE,
          row);
    }
    drawFooter(context, textRenderer, view);
  }

  private static void drawEmpty(
      DrawContext context, TextRenderer textRenderer, NetworkTagView view) {
    final Text message = TerminalScreenText.of(view.answered() ? "tags.none" : "settings.pending");
    final List<OrderedText> lines =
        textRenderer.wrapLines(message, TerminalSettingsLayout.LIST_WIDTH - 8);
    int top =
        TerminalSettingsLayout.LIST_Y
            + (TagsSectionLayout.LIST_HEIGHT - lines.size() * LINE_HEIGHT) / 2;
    final int centre = TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH / 2;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          centre - textRenderer.getWidth(line) / 2,
          top,
          VanillaPalette.opaque(VanillaPalette.MUTED),
          false);
      top += LINE_HEIGHT;
    }
  }

  private static void drawFooter(
      DrawContext context, TextRenderer textRenderer, NetworkTagView view) {
    final Text footer =
        TerminalScreenText.of(view.mayAdminister() ? "tags.presets_fixed" : "tags.read_only");
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(
            textRenderer,
            footer,
            TerminalSettingsLayout.CONTENT_RIGHT - TerminalSettingsLayout.CONTENT_X),
        TerminalSettingsLayout.CONTENT_X,
        TagsSectionLayout.FOOTER_Y,
        VanillaPalette.opaque(VanillaPalette.MUTED),
        false);
  }

  private void drawRow(
      DrawContext context,
      TextRenderer textRenderer,
      boolean mayAdminister,
      NetworkTagEntry tag,
      boolean hovered,
      TagRowRegion region,
      int row) {
    final int top = TagsSectionLayout.rowY(row);
    final int left = TerminalSettingsLayout.LIST_X;
    final boolean armed = mayAdminister && isArmed(tag.id());
    final boolean controls = mayAdminister && hovered && !armed;
    if (hovered && mayAdminister) {
      TerminalChrome.drawHover(
          context, left, top, TerminalSettingsLayout.LIST_WIDTH, TagsSectionLayout.ROW_HEIGHT);
    }
    int right = left + TerminalSettingsLayout.LIST_WIDTH - TagsSectionLayout.TEXT_MARGIN;
    if (armed) {
      drawArmed(context, textRenderer, region == TagRowRegion.DELETE, row);
      right = TagsSectionLayout.ARMED_X - TagsSectionLayout.COUNT_GAP;
    } else if (controls) {
      drawControl(
          context,
          TerminalSprite.RENAME,
          TagsSectionLayout.RENAME_X,
          row,
          region == TagRowRegion.RENAME);
      drawControl(
          context,
          TerminalSprite.DELETE,
          TagsSectionLayout.DELETE_X,
          row,
          region == TagRowRegion.DELETE);
      right = TagsSectionLayout.RENAME_X - TagsSectionLayout.COUNT_GAP;
    }
    final Text count = CountedNoun.ITEMS.of(tag.assignedItems());
    final int countWidth = textRenderer.getWidth(count);
    context.drawText(
        textRenderer,
        count,
        right - countWidth,
        top + TEXT_Y,
        VanillaPalette.opaque(VanillaPalette.MUTED),
        false);
    final int nameLeft = left + TagsSectionLayout.TEXT_MARGIN;
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(
            textRenderer,
            FilterColumnText.nameOf(tag),
            right - countWidth - TagsSectionLayout.COUNT_GAP - nameLeft),
        nameLeft,
        top + TEXT_Y,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }

  private static void drawControl(
      DrawContext context, TerminalSprite icon, int x, int row, boolean hovered) {
    final int y = TagsSectionLayout.controlY(row);
    (hovered ? TerminalSprite.RAISED_LIT : TerminalSprite.RAISED)
        .draw(context, x, y, TagsSectionLayout.CONTROL_SIZE, TagsSectionLayout.CONTROL_SIZE);
    final int inset = (TagsSectionLayout.CONTROL_SIZE - TerminalIconButton.ICON_SIZE) / 2;
    icon.draw(
        context, x + inset, y + inset, TerminalIconButton.ICON_SIZE, TerminalIconButton.ICON_SIZE);
  }

  private static void drawArmed(
      DrawContext context, TextRenderer textRenderer, boolean hovered, int row) {
    final int x = TagsSectionLayout.ARMED_X;
    final int y = TagsSectionLayout.armedY(row);
    VanillaSprite.button(true, hovered)
        .draw(context, x, y, TagsSectionLayout.ARMED_WIDTH, TagsSectionLayout.ARMED_HEIGHT);
    TerminalChrome.fill(
        context,
        x + TerminalTextButton.faceInset(),
        y + TerminalTextButton.faceInset(),
        TagsSectionLayout.ARMED_WIDTH - 2 * TerminalTextButton.faceInset(),
        TagsSectionLayout.ARMED_HEIGHT - 2 * TerminalTextButton.faceInset() - 1,
        VanillaPalette.ACCENT);
    final Text label = TerminalScreenText.of("tags.delete.armed");
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, label, TagsSectionLayout.ARMED_WIDTH - 4),
        x + (TagsSectionLayout.ARMED_WIDTH - textRenderer.getWidth(label) + 1) / 2,
        y + (TagsSectionLayout.ARMED_HEIGHT - 8) / 2,
        VanillaPalette.opaque(VanillaPalette.BUTTON_TEXT),
        true);
  }

  public Text tooltipAt(NetworkTagView view, int firstVisible, int windowX, int windowY) {
    final NetworkTagEntry tag = tagAt(view, firstVisible, windowX, windowY);
    if (tag == null || !view.mayAdminister()) {
      return null;
    }
    return switch (regionAt(view, firstVisible, windowX, windowY)) {
      case RENAME -> TerminalScreenText.of("tags.rename");
      case DELETE ->
          TerminalScreenText.of(isArmed(tag.id()) ? "tags.delete.confirm" : "tags.delete");
      case NAME, NONE -> null;
    };
  }
}
