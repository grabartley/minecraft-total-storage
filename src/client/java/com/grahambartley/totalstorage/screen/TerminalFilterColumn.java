package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import com.grahambartley.totalstorage.text.CountedNoun;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TerminalFilterColumn {
  private static final int TEXT_Y = 2;

  private final ListingScroll scroll = new ListingScroll(1, FilterColumnLayout.ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, FilterColumnLayout.CONTENT_HEIGHT);

  private FilterColumnRows rows = new FilterColumnRows(List.of());

  private TerminalTextButton mode;
  private TerminalIconButton clear;

  public ListingScroll scroll() {
    return scroll;
  }

  public void attach(
      Consumer<ClickableWidget> adder, int originX, int originY, FilterSelection selection) {
    mode =
        new TerminalTextButton(
            originX + FilterColumnLayout.X,
            originY + FilterColumnLayout.Y,
            FilterColumnLayout.MODE_WIDTH,
            FilterColumnLayout.HEADER_HEIGHT,
            FilterColumnText.mode(selection.mode()),
            selection::cycleMode);
    mode.setHint(TerminalScreenText.of("filter.mode.hint"));
    clear =
        new TerminalIconButton(
            originX + FilterColumnLayout.CLEAR_X,
            originY + FilterColumnLayout.Y,
            FilterColumnLayout.CONTROL_SIZE,
            TerminalSprite.CROSS,
            TerminalSprite.CROSS_UNAVAILABLE,
            TerminalScreenText.of("filter.clear"),
            selection::clear);
    adder.accept(mode);
    adder.accept(clear);
  }

  public void update(boolean shown, FilterSelection selection) {
    mode.visible = shown;
    clear.visible = shown;
    mode.show(FilterColumnText.mode(selection.mode()), false);
    clear.active = selection.isFiltering();
  }

  public void fit(NetworkTagView view) {
    rows = FilterColumnRows.of(view);
    scroll.fit(rows.size());
  }

  public NetworkTagEntry tagAt(int windowX, int windowY) {
    return rows.tagAt(rowAt(windowX, windowY));
  }

  public int rowAt(int windowX, int windowY) {
    final int row = FilterColumnLayout.rowAt(windowX, windowY);
    return row == FilterColumnLayout.NO_ROW ? row : scroll.firstIndex() + row;
  }

  public boolean isDraggingScrollbar() {
    return scrollbar.isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - FilterColumnLayout.CONTENT_Y);
  }

  public void releaseScrollbar() {
    scrollbar.release();
  }

  public boolean click(FilterSelection selection, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !FilterColumnLayout.isOverColumn(windowX, windowY)) {
      return false;
    }
    if (FilterColumnLayout.isOverScrollbar(windowX, windowY)) {
      scrollbar.grab(windowY - FilterColumnLayout.CONTENT_Y);
      return true;
    }
    final NetworkTagEntry tag = tagAt(windowX, windowY);
    if (tag != null) {
      selection.toggle(tag.id());
    }
    return windowY >= FilterColumnLayout.FRAME_Y;
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      FilterSelection selection,
      int mouseX,
      int mouseY) {
    TerminalChrome.drawInset(
        context,
        FilterColumnLayout.X,
        FilterColumnLayout.FRAME_Y,
        FilterColumnLayout.WIDTH,
        FilterColumnLayout.FRAME_HEIGHT);
    final int hovered = rowAt(mouseX, mouseY);
    for (int row = 0; row < FilterColumnLayout.ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      final FilterColumnRows.Row shown = rows.at(index);
      if (shown == null) {
        break;
      }
      final int top = FilterColumnLayout.rowY(row);
      if (shown.isDivider()) {
        drawDivider(context, textRenderer, top);
        continue;
      }
      drawRow(
          context,
          textRenderer,
          shown.tag(),
          selection.isActive(shown.tag().id()),
          index == hovered,
          top);
    }
    TerminalChrome.drawScrollbar(
        context,
        scrollbar,
        FilterColumnLayout.SCROLLBAR_X,
        FilterColumnLayout.CONTENT_Y,
        FilterColumnLayout.SCROLLBAR_WIDTH,
        FilterColumnLayout.CONTENT_HEIGHT);
  }

  private static void drawDivider(DrawContext context, TextRenderer textRenderer, int top) {
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(
            textRenderer,
            TerminalScreenText.of("filter.custom"),
            FilterColumnLayout.ROW_WIDTH - 2 * FilterColumnLayout.TEXT_MARGIN),
        FilterColumnLayout.ROW_X + FilterColumnLayout.TEXT_MARGIN,
        top + TEXT_Y + 1,
        VanillaPalette.opaque(VanillaPalette.MUTED),
        false);
  }

  private static void drawRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagEntry tag,
      boolean active,
      boolean hovered,
      int top) {
    final int left = FilterColumnLayout.ROW_X;
    if (active) {
      TerminalSprite.SUNKEN.draw(
          context, left, top, FilterColumnLayout.ROW_WIDTH, FilterColumnLayout.ROW_HEIGHT);
      TerminalSprite.CHECK_LIGHT.draw(
          context,
          left + FilterColumnLayout.TEXT_MARGIN,
          top + (FilterColumnLayout.ROW_HEIGHT - FilterColumnLayout.CHECK_SIZE) / 2,
          FilterColumnLayout.CHECK_SIZE,
          FilterColumnLayout.CHECK_SIZE);
    } else if (hovered) {
      TerminalChrome.drawHover(
          context, left, top, FilterColumnLayout.ROW_WIDTH, FilterColumnLayout.ROW_HEIGHT);
    }
    final String count = FilterColumnText.count(tag);
    final int countWidth = textRenderer.getWidth(count);
    final OrderedText name =
        TrimmedText.toWidth(
            textRenderer,
            FilterColumnText.nameOf(tag),
            FilterColumnLayout.nameWidth(countWidth, active));
    final int nameColour =
        active
            ? VanillaPalette.BEVEL_HIGHLIGHT
            : tag.isEmptyHere() ? VanillaPalette.DISABLED : VanillaPalette.LABEL;
    final int countColour =
        active
            ? VanillaPalette.FIELD_TEXT
            : tag.isEmptyHere() ? VanillaPalette.DISABLED : VanillaPalette.MUTED;
    context.drawText(
        textRenderer,
        name,
        left + FilterColumnLayout.nameLeft(active),
        top + TEXT_Y,
        VanillaPalette.opaque(nameColour),
        active);
    context.drawText(
        textRenderer,
        count,
        left + FilterColumnLayout.countLeft(countWidth),
        top + TEXT_Y,
        VanillaPalette.opaque(countColour),
        active);
  }

  public List<Text> tooltipAt(int windowX, int windowY) {
    final NetworkTagEntry tag = tagAt(windowX, windowY);
    if (tag == null) {
      return List.of();
    }
    return List.of(
        FilterColumnText.nameOf(tag),
        TerminalScreenText.of(
                "filter.tag.matching",
                CountedNoun.KINDS.ofFormatted(FilterColumnText.count(tag), tag.matchingItems()))
            .formatted(Formatting.GRAY));
  }
}
