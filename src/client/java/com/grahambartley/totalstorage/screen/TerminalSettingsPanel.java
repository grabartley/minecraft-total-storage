package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.screenhandler.TagsSectionLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TerminalSettingsPanel {
  private static final int LINE_HEIGHT = 9;

  private static final int LABEL_OFFSET = 2;

  private static final int DESCRIPTION_OFFSET = 11;

  private final ListingScroll scroll = new ListingScroll(1, TerminalSettingsLayout.ROWS);

  private final ListingPrefetch prefetch =
      new ListingPrefetch(scroll, NetworkSettingsView.MAX_CONTAINERS);

  private final TerminalScrollbar containerScrollbar =
      new TerminalScrollbar(scroll, TerminalSettingsLayout.LIST_HEIGHT);

  private final TerminalScrollbar tagScrollbar =
      new TerminalScrollbar(scroll, TagsSectionLayout.LIST_HEIGHT);

  private SettingsSection section = SettingsSection.TERMINAL;

  public SettingsSection section() {
    return section;
  }

  public boolean scrolls() {
    return section != SettingsSection.TERMINAL;
  }

  public void show(SettingsSection shown) {
    section = shown;
    scroll.scrollTo(0);
    containerScrollbar.release();
    tagScrollbar.release();
  }

  public ListingScroll scroll() {
    return scroll;
  }

  public ListingWindow containersToAskFor(boolean settingsOpen) {
    return showsContainers(settingsOpen) ? prefetch.window() : ListingWindow.EMPTY;
  }

  public ListingWindow containersToKeep(boolean settingsOpen) {
    return showsContainers(settingsOpen) ? prefetch.needed() : ListingWindow.EMPTY;
  }

  private boolean showsContainers(boolean settingsOpen) {
    return settingsOpen && section == SettingsSection.CONTAINERS;
  }

  private TerminalScrollbar scrollbar() {
    return section == SettingsSection.TAGS ? tagScrollbar : containerScrollbar;
  }

  private int trackHeight() {
    return section == SettingsSection.TAGS
        ? TagsSectionLayout.LIST_HEIGHT
        : TerminalSettingsLayout.LIST_HEIGHT;
  }

  public boolean isOverScrollbar(int windowX, int windowY) {
    return scrolls()
        && windowX >= TerminalSettingsLayout.SCROLLBAR_X
        && windowX < TerminalSettingsLayout.SCROLLBAR_X + TerminalSettingsLayout.SCROLLBAR_WIDTH
        && windowY >= TerminalSettingsLayout.LIST_Y
        && windowY < TerminalSettingsLayout.LIST_Y + trackHeight();
  }

  public boolean isOverList(int windowX, int windowY) {
    return scrolls()
        && windowX >= TerminalSettingsLayout.LIST_X
        && windowX < TerminalSettingsLayout.SCROLLBAR_X + TerminalSettingsLayout.SCROLLBAR_WIDTH
        && windowY >= TerminalSettingsLayout.LIST_Y
        && windowY < TerminalSettingsLayout.LIST_Y + trackHeight();
  }

  public boolean grabScrollbar(int windowY) {
    return scrolls() && scrollbar().grab(windowY - TerminalSettingsLayout.LIST_Y);
  }

  public boolean isDraggingScrollbar() {
    return scrolls() && scrollbar().isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar().dragTo(windowY - TerminalSettingsLayout.LIST_Y);
  }

  public void releaseScrollbar() {
    containerScrollbar.release();
    tagScrollbar.release();
  }

  public void fit(NetworkSettingsView view, int customTags) {
    scroll.fit(
        switch (section) {
          case TERMINAL -> 0;
          case CONTAINERS -> view.containerTotal();
          case TAGS -> customTags;
        });
  }

  public int containerRowAt(int windowX, int windowY) {
    if (section != SettingsSection.CONTAINERS) {
      return TerminalSettingsLayout.NO_ROW;
    }
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    return row == TerminalSettingsLayout.NO_ROW ? row : scroll.firstIndex() + row;
  }

  public void drawBackground(
      DrawContext context, int originX, int originY, int mouseX, int mouseY) {
    final int hovered = TerminalSettingsLayout.sectionTabAt(mouseX, mouseY);
    for (final SettingsSection shown : SettingsSection.values()) {
      if (shown != section) {
        TerminalChrome.drawSectionTab(
            context,
            originX + TerminalSettingsLayout.sectionTabX(shown.index()),
            originY + TerminalSettingsLayout.SECTION_TAB_Y,
            TerminalSettingsLayout.SECTION_TAB_WIDTH,
            TerminalSettingsLayout.SECTION_TAB_HEIGHT,
            false);
        if (hovered == shown.index()) {
          TerminalChrome.drawHover(
              context,
              originX + TerminalSettingsLayout.sectionTabX(shown.index()) + 2,
              originY + TerminalSettingsLayout.SECTION_TAB_Y + 2,
              TerminalSettingsLayout.SECTION_TAB_WIDTH - 4,
              TerminalSettingsLayout.SECTION_TAB_HEIGHT - 3);
        }
      }
    }
    TerminalChrome.drawRaisedFrame(
        context,
        originX + TerminalSettingsLayout.FRAME_X,
        originY + TerminalSettingsLayout.FRAME_Y,
        TerminalSettingsLayout.FRAME_WIDTH,
        TerminalSettingsLayout.FRAME_HEIGHT);
    TerminalChrome.drawSectionTab(
        context,
        originX + TerminalSettingsLayout.sectionTabX(section.index()),
        originY + TerminalSettingsLayout.SELECTED_TAB_Y,
        TerminalSettingsLayout.SECTION_TAB_WIDTH,
        TerminalSettingsLayout.SELECTED_TAB_HEIGHT,
        true);
    if (scrolls() && scroll.totalRows() > 0) {
      TerminalChrome.drawScrollbar(
          context,
          scrollbar(),
          originX + TerminalSettingsLayout.SCROLLBAR_X,
          originY + TerminalSettingsLayout.LIST_Y,
          TerminalSettingsLayout.SCROLLBAR_WIDTH,
          trackHeight());
    }
  }

  public void drawForeground(
      DrawContext context, TextRenderer textRenderer, NetworkSettingsView view) {
    drawSectionLabels(context, textRenderer);
    if (section == SettingsSection.TERMINAL) {
      drawTerminalSection(context, textRenderer, view);
    }
  }

  private void drawSectionLabels(DrawContext context, TextRenderer textRenderer) {
    for (final SettingsSection shown : SettingsSection.values()) {
      final boolean selected = shown == section;
      final int left = TerminalSettingsLayout.sectionTabX(shown.index());
      final int top =
          selected
              ? TerminalSettingsLayout.SELECTED_TAB_Y + 4
              : TerminalSettingsLayout.SECTION_TAB_Y + 3;
      final int room = TerminalSettingsLayout.SECTION_TAB_WIDTH - 8;
      final var label =
          TrimmedText.toWidth(textRenderer, TerminalScreenText.of(shown.label()), room);
      context.drawText(
          textRenderer,
          label,
          left + (TerminalSettingsLayout.SECTION_TAB_WIDTH - textRenderer.getWidth(label) + 1) / 2,
          top,
          VanillaPalette.opaque(selected ? VanillaPalette.LABEL : VanillaPalette.BEVEL_HIGHLIGHT),
          !selected);
    }
  }

  private static void drawTerminalSection(
      DrawContext context, TextRenderer textRenderer, NetworkSettingsView view) {
    for (final TerminalSetting setting : TerminalSetting.values()) {
      final int top = TerminalSettingsLayout.settingY(setting.row());
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(
              textRenderer,
              TerminalScreenText.of(setting.label()),
              TerminalSettingsLayout.DESCRIPTION_WIDTH),
          TerminalSettingsLayout.CONTENT_X,
          top + LABEL_OFFSET,
          VanillaPalette.opaque(VanillaPalette.LABEL),
          false);
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(
              textRenderer,
              TerminalSettingsText.description(setting, view),
              TerminalSettingsLayout.DESCRIPTION_WIDTH),
          TerminalSettingsLayout.CONTENT_X,
          top + DESCRIPTION_OFFSET,
          VanillaPalette.opaque(VanillaPalette.MUTED),
          false);
    }
    final Text radius = TerminalSettingsText.radiusValue(view);
    final int valueLeft =
        TerminalSettingsButtons.RADIUS_DOWN_X + TerminalSettingsButtons.STEPPER_SIZE;
    final int valueWidth = TerminalSettingsButtons.RADIUS_UP_X - valueLeft;
    final var shown = TrimmedText.toWidth(textRenderer, radius, valueWidth - 4);
    context.drawText(
        textRenderer,
        shown,
        valueLeft + (valueWidth - textRenderer.getWidth(shown) + 1) / 2,
        TerminalSettingsLayout.settingY(TerminalSetting.RADIUS.row())
            + (TerminalSettingsLayout.BUTTON_HEIGHT - LINE_HEIGHT) / 2
            + 1,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }

  public Text descriptionTooltipAt(
      TextRenderer textRenderer, NetworkSettingsView view, int windowX, int windowY) {
    if (section != SettingsSection.TERMINAL
        || windowX < TerminalSettingsLayout.CONTENT_X
        || windowX >= TerminalSettingsLayout.CONTENT_X + TerminalSettingsLayout.DESCRIPTION_WIDTH) {
      return null;
    }
    for (final TerminalSetting setting : TerminalSetting.values()) {
      final int top = TerminalSettingsLayout.settingY(setting.row()) + DESCRIPTION_OFFSET;
      if (windowY >= top && windowY < top + LINE_HEIGHT) {
        final Text description = TerminalSettingsText.description(setting, view);
        return textRenderer.getWidth(description) > TerminalSettingsLayout.DESCRIPTION_WIDTH
            ? description
            : null;
      }
    }
    return null;
  }
}
