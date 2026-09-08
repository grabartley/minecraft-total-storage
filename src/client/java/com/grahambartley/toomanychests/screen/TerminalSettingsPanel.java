package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Draws how a network is configured, and says what a click landed on.
 *
 * <p>Two sections behind a strip along the top, because the recess is a hundred pixels tall and the
 * window cannot grow: sharing that between the terminal's own settings and its container list makes
 * both unusable, where splitting it costs fourteen pixels once and leaves each section room to
 * breathe. See {@link TerminalSettingsLayout#SECTION_COUNT}.
 *
 * <p>Every row is twenty pixels, which is what lets a container row carry a real item icon and a
 * setting row carry a control big enough to aim at. A container is recognised by what is in it far
 * sooner than by its coordinates, so the icon leads and the position is left to the tooltip.
 *
 * <p>Everything shown comes from the server's answer rather than from anything worked out here. The
 * client does not know the server's radius cap, cannot turn a trust list into names, and never sees
 * the block entity the roles live on.
 *
 * <p>Controls a player may not use are drawn flat rather than hidden. A missing button reads as a
 * broken screen; a disabled one reads as somebody else's terminal, which is the truth.
 */
public final class TerminalSettingsPanel {
  private static final Identifier TAB_SELECTED = TerminalSprites.of("filter/row_active");
  private static final Identifier TAB_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier TAB = TerminalSprites.of("filter/row");
  private static final Identifier ROW = TerminalSprites.of("filter/row");
  private static final Identifier ROW_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier ROW_ACTIVE = TerminalSprites.of("filter/row_active");
  private static final Identifier CONTROL = TerminalSprites.of("button/normal");
  private static final Identifier CONTROL_HOVER = TerminalSprites.of("button/hover");
  private static final Identifier CONTROL_DISABLED = TerminalSprites.of("button/disabled");
  private static final Identifier SCROLL_TRACK = TerminalSprites.of("scroll/track");
  private static final Identifier SCROLL_THUMB = TerminalSprites.of("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED =
      TerminalSprites.of("scroll/thumb_disabled");

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int MUTED_COLOUR = 0x9AA3B3;

  /** The colour a network close to full is written in, matching the excluded role's warning red. */
  private static final int WARNING_COLOUR = 0xED6353;

  private static final int TEXT_INSET = 4;
  private static final int ICON_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  /** How much of a row's right edge the role chip is given, measured from the row's end. */
  private static final int CHIP_WIDTH = 52;

  private final ListingScroll scroll = new ListingScroll(1, TerminalSettingsLayout.ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TerminalSettingsLayout.CONTENT_HEIGHT);

  private SettingsSection section = SettingsSection.TERMINAL;

  public SettingsSection section() {
    return section;
  }

  /**
   * Whether the open section has a list to scroll, which only the container one does.
   *
   * <p>Asked before the scrollbar is offered a click or a wheel notch. The scrollbar refusing on
   * its own is not enough: it refuses by asking whether it overflows, and between switching section
   * and the next answer from the server that is still the other section's count.
   */
  public boolean scrolls() {
    return section == SettingsSection.CONTAINERS;
  }

  public void show(SettingsSection shown) {
    section = shown;
    scroll.scrollTo(0);
    scrollbar.release();
  }

  public ListingScroll scroll() {
    return scroll;
  }

  /**
   * Whether a point is over a scrollbar this panel is actually showing.
   *
   * <p>The section is folded in here rather than asked beside every call, because a settings row
   * spans the whole panel and the stepper that raises the radius reaches into the band the
   * container list keeps for its own scrollbar. Every way in has to agree about that, and a rule
   * spelled out at four call sites is a rule three of them will keep and one will not.
   */
  public boolean isOverScrollbar(int windowX, int windowY) {
    return scrolls() && TerminalSettingsLayout.isOverScrollbar(windowX, windowY);
  }

  /** Takes hold of the thumb, measured from the top of the window rather than of the track. */
  public boolean grabScrollbar(int windowY) {
    return scrolls() && scrollbar.grab(windowY - TerminalSettingsLayout.CONTENT_Y);
  }

  /** Whether a drag begun on this panel's scrollbar is still running. */
  public boolean isDraggingScrollbar() {
    return scrolls() && scrollbar.isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - TerminalSettingsLayout.CONTENT_Y);
  }

  public void releaseScrollbar() {
    scrollbar.release();
  }

  TerminalScrollbar scrollbar() {
    return scrollbar;
  }

  /** Keeps the scroll inside a list that may have changed size since the last answer. */
  public void fit(NetworkSettingsView view) {
    scroll.fit(section == SettingsSection.CONTAINERS ? view.containerTotal() : 0);
  }

  /**
   * The row of the whole container list a screen position is over, or {@link
   * TerminalSettingsLayout#NO_ROW} for none.
   */
  public int containerRowAt(int windowX, int windowY) {
    if (section != SettingsSection.CONTAINERS
        || TerminalSettingsLayout.isOverScrollbar(windowX, windowY)) {
      return TerminalSettingsLayout.NO_ROW;
    }
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    return row == TerminalSettingsLayout.NO_ROW ? row : scroll.firstIndex() + row;
  }

  /** The terminal setting a screen position is over, or null for none. */
  public TerminalSetting settingAt(int windowX, int windowY) {
    if (section != SettingsSection.TERMINAL) {
      return null;
    }
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    return row == TerminalSettingsLayout.NO_ROW ? null : TerminalSetting.atRow(row);
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    drawSectionTabs(context, textRenderer, originX, originY, mouseX, mouseY);
    if (section == SettingsSection.TERMINAL) {
      drawTerminalSection(context, textRenderer, view, originX, originY, mouseX, mouseY);
      return;
    }
    drawContainerSection(context, textRenderer, view, originX, originY, mouseX, mouseY);
  }

  private void drawSectionTabs(
      DrawContext context,
      TextRenderer textRenderer,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final int hovered = TerminalSettingsLayout.sectionTabAt(mouseX, mouseY);
    for (final SettingsSection shown : SettingsSection.values()) {
      final int left = originX + TerminalSettingsLayout.sectionTabX(shown.index());
      final int top = originY + TerminalSettingsLayout.PANEL_Y;
      context.drawGuiTexture(
          shown == section ? TAB_SELECTED : hovered == shown.index() ? TAB_HOVER : TAB,
          left,
          top,
          TerminalSettingsLayout.SECTION_TAB_WIDTH,
          TerminalSettingsLayout.SECTION_TAB_HEIGHT);
      final Text label = TerminalScreenText.of(shown.label());
      context.drawText(
          textRenderer,
          label,
          left + (TerminalSettingsLayout.SECTION_TAB_WIDTH - textRenderer.getWidth(label)) / 2,
          top + (TerminalSettingsLayout.SECTION_TAB_HEIGHT - LINE_HEIGHT) / 2 + 1,
          shown == section ? LABEL_COLOUR : MUTED_COLOUR,
          false);
    }
  }

  private void drawTerminalSection(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final TerminalSetting hovered = settingAt(mouseX, mouseY);
    final SettingsControl control = SettingsControl.at(mouseX, mouseY);
    drawRow(context, TerminalSetting.RADIUS, originX, originY, false, false);
    stepper(
        context,
        textRenderer,
        "-",
        originX + SettingsControl.radiusDownX(),
        originY + SettingsControl.radiusY(),
        view.mayAdminister() && view.radius() > view.minRadius(),
        control == SettingsControl.RADIUS_DOWN);
    stepper(
        context,
        textRenderer,
        "+",
        originX + SettingsControl.radiusUpX(),
        originY + SettingsControl.radiusY(),
        view.mayAdminister() && view.radius() < view.maxRadius(),
        control == SettingsControl.RADIUS_UP);
    centred(
        context,
        textRenderer,
        view.isClamped()
            ? TerminalScreenText.of(
                "settings.radius.clamped", view.radius(), view.effectiveRadius())
            : TerminalScreenText.of(
                TerminalSetting.RADIUS.label(), view.radius(), view.maxRadius()),
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.RADIUS.row()),
        LABEL_COLOUR);

    drawRow(
        context,
        TerminalSetting.ACCESS,
        originX,
        originY,
        hovered == TerminalSetting.ACCESS && view.mayAdminister(),
        false);
    labelled(
        context,
        textRenderer,
        TerminalScreenText.of(TerminalSetting.ACCESS.label()),
        TerminalScreenText.of("access." + view.accessMode().id()),
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.ACCESS.row()),
        view.mayAdminister());

    drawRow(context, TerminalSetting.TRUSTED, originX, originY, false, false);
    labelled(
        context,
        textRenderer,
        TerminalScreenText.of(TerminalSetting.TRUSTED.label()),
        view.trusted().isEmpty()
            ? TerminalScreenText.of("settings.trusted.none")
            : Text.literal(String.join(", ", view.trusted())),
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.TRUSTED.row()),
        false);

    drawRow(context, TerminalSetting.CAPACITY, originX, originY, false, false);
    capacity(
        context,
        textRenderer,
        view,
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.CAPACITY.row()));
  }

  /**
   * How much room is left, in slots, turning warning-coloured once the network is as full as the
   * server's threshold. Slots rather than items, because slots are what a deposit runs out of.
   */
  private static void capacity(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int rowY) {
    final NetworkCapacity room = view.capacity();
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int textY = rowY + (TerminalSettingsLayout.ROW_HEIGHT - 1 - LINE_HEIGHT) / 2;
    context.drawText(
        textRenderer,
        TerminalScreenText.of(TerminalSetting.CAPACITY.label()),
        left + TEXT_INSET,
        textY,
        MUTED_COLOUR,
        false);
    final Text value =
        room.isUnknown()
            ? TerminalScreenText.of("settings.capacity.unknown")
            : TerminalScreenText.of("settings.capacity.slots", room.freeSlots(), room.totalSlots());
    context.drawText(
        textRenderer,
        value,
        left + TerminalSettingsLayout.FULL_ROW_WIDTH - TEXT_INSET - textRenderer.getWidth(value),
        textY,
        room.isNearCapacity(ClientConfigMirror.current().nearCapacityWarningPercent())
            ? WARNING_COLOUR
            : LABEL_COLOUR,
        false);
  }

  private void drawContainerSection(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    drawScrollbar(context, originX, originY);
    if (!view.hasContainers()) {
      centred(
          context,
          textRenderer,
          TerminalScreenText.of(view.powered() ? "settings.empty" : "settings.offline"),
          originX,
          originY + TerminalSettingsLayout.rowY(0),
          MUTED_COLOUR);
      return;
    }
    final int hovered = containerRowAt(mouseX, mouseY);
    for (int row = 0; row < TerminalSettingsLayout.ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      final ContainerRoleEntry entry = view.at(index);
      if (entry != null) {
        drawContainerRow(
            context,
            textRenderer,
            entry,
            originX,
            originY,
            row,
            index == hovered && view.mayAdminister(),
            view.mayAdminister());
      }
    }
  }

  private static void drawContainerRow(
      DrawContext context,
      TextRenderer textRenderer,
      ContainerRoleEntry entry,
      int originX,
      int originY,
      int row,
      boolean hovered,
      boolean changeable) {
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int top = originY + TerminalSettingsLayout.rowY(row);
    context.drawGuiTexture(
        hovered ? ROW_HOVER : entry.role() == ContainerRole.EXCLUDED ? ROW_ACTIVE : ROW,
        left,
        top,
        TerminalSettingsLayout.ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT - 1);
    final int iconY = top + (TerminalSettingsLayout.ROW_HEIGHT - 1 - ICON_SIZE) / 2;
    entry.icon().ifPresent(icon -> context.drawItem(icon.toStack(), left + TEXT_INSET, iconY));
    final int textY = top + (TerminalSettingsLayout.ROW_HEIGHT - 1 - LINE_HEIGHT) / 2;
    context.drawText(
        textRenderer,
        ContainerRowText.contents(entry),
        left + TEXT_INSET + ICON_SIZE + TEXT_INSET,
        textY,
        changeable ? LABEL_COLOUR : MUTED_COLOUR,
        false);
    final Text chip = ContainerRowText.role(entry.role());
    context.drawText(
        textRenderer,
        chip,
        left + TerminalSettingsLayout.ROW_WIDTH - CHIP_WIDTH,
        textY,
        changeable ? ContainerRowText.roleColour(entry.role()) : MUTED_COLOUR,
        false);
  }

  private static void drawRow(
      DrawContext context,
      TerminalSetting setting,
      int originX,
      int originY,
      boolean hovered,
      boolean active) {
    context.drawGuiTexture(
        active ? ROW_ACTIVE : hovered ? ROW_HOVER : ROW,
        originX + TerminalSettingsLayout.PANEL_X,
        originY + TerminalSettingsLayout.rowY(setting.row()),
        TerminalSettingsLayout.FULL_ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT - 1);
  }

  /** A row reading as a name on the left and its value on the right, which is what a setting is. */
  private static void labelled(
      DrawContext context,
      TextRenderer textRenderer,
      Text name,
      Text value,
      int originX,
      int rowY,
      boolean changeable) {
    final int left = originX + TerminalSettingsLayout.PANEL_X;
    final int textY = rowY + (TerminalSettingsLayout.ROW_HEIGHT - 1 - LINE_HEIGHT) / 2;
    context.drawText(textRenderer, name, left + TEXT_INSET, textY, MUTED_COLOUR, false);
    final int room =
        TerminalSettingsLayout.FULL_ROW_WIDTH - 2 * TEXT_INSET - textRenderer.getWidth(name) - 4;
    final List<OrderedText> lines = textRenderer.wrapLines(value, Math.max(1, room));
    if (lines.isEmpty()) {
      return;
    }
    context.drawText(
        textRenderer,
        lines.get(0),
        left
            + TerminalSettingsLayout.FULL_ROW_WIDTH
            - TEXT_INSET
            - textRenderer.getWidth(lines.get(0)),
        textY,
        changeable ? LABEL_COLOUR : MUTED_COLOUR,
        false);
  }

  /** Centred between the two controls that flank it, which is where the radius reads best. */
  private static void centred(
      DrawContext context,
      TextRenderer textRenderer,
      Text text,
      int originX,
      int rowY,
      int colour) {
    final int room = TerminalSettingsLayout.FULL_ROW_WIDTH;
    final List<OrderedText> lines = textRenderer.wrapLines(text, room - 2 * SettingsControl.size());
    if (lines.isEmpty()) {
      return;
    }
    context.drawText(
        textRenderer,
        lines.get(0),
        originX + TerminalSettingsLayout.PANEL_X + (room - textRenderer.getWidth(lines.get(0))) / 2,
        rowY + (TerminalSettingsLayout.ROW_HEIGHT - 1 - LINE_HEIGHT) / 2,
        colour,
        false);
  }

  /**
   * A square button carrying one glyph, sized so it can be aimed at rather than hunted for. Drawn
   * rather than added as a widget so it tracks the row it belongs to and leaves with its section.
   */
  private static void stepper(
      DrawContext context,
      TextRenderer textRenderer,
      String glyph,
      int x,
      int y,
      boolean enabled,
      boolean hovered) {
    final int size = SettingsControl.size();
    context.drawGuiTexture(
        !enabled ? CONTROL_DISABLED : hovered ? CONTROL_HOVER : CONTROL, x, y, size, size);
    context.drawText(
        textRenderer,
        glyph,
        x + (size - textRenderer.getWidth(glyph)) / 2,
        y + (size - LINE_HEIGHT) / 2 + 1,
        enabled ? LABEL_COLOUR : MUTED_COLOUR,
        false);
  }

  /** The list's own scrollbar, drawn in the gutter the rows leave for it. */
  private void drawScrollbar(DrawContext context, int originX, int originY) {
    context.drawGuiTexture(
        SCROLL_TRACK,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalSettingsLayout.CONTENT_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalSettingsLayout.CONTENT_HEIGHT);
    context.drawGuiTexture(
        scrollbar.isEnabled() ? SCROLL_THUMB : SCROLL_THUMB_DISABLED,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalSettingsLayout.CONTENT_Y + scrollbar.thumbTop(),
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        scrollbar.thumbHeight());
  }
}
