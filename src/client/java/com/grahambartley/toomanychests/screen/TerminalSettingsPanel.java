package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.screenhandler.TagsSectionLayout;
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

public final class TerminalSettingsPanel {
  private static final int TEXT_INSET = 4;
  private static final int ICON_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  private static final int CHIP_WIDTH = 52;

  private final ListingScroll scroll = new ListingScroll(1, TerminalSettingsLayout.ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TerminalSettingsLayout.CONTENT_HEIGHT);

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
    scrollbar.release();
  }

  public ListingScroll scroll() {
    return scroll;
  }

  public boolean isOverScrollbar(int windowX, int windowY) {
    return scrolls() && TerminalSettingsLayout.isOverScrollbar(windowX, windowY);
  }

  public boolean grabScrollbar(int windowY) {
    return scrolls() && scrollbar.grab(windowY - TerminalSettingsLayout.CONTENT_Y);
  }

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

  public void fit(NetworkSettingsView view, int customTags) {
    scroll.fit(
        switch (section) {
          case TERMINAL -> 0;
          case CONTAINERS -> view.containerTotal();
          case TAGS -> customTags + TagsSectionLayout.FIRST_TAG_ROW;
        });
  }

  public int containerRowAt(int windowX, int windowY) {
    if (section != SettingsSection.CONTAINERS
        || TerminalSettingsLayout.isOverScrollbar(windowX, windowY)) {
      return TerminalSettingsLayout.NO_ROW;
    }
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    return row == TerminalSettingsLayout.NO_ROW ? row : scroll.firstIndex() + row;
  }

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
    if (section == SettingsSection.TAGS) {
      drawScrollbar(context, originX, originY);
      return;
    }
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
      TerminalChrome.tab(shown == section, hovered == shown.index())
          .draw(
              context,
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
          shown == section ? VanillaPalette.LABEL : VanillaPalette.MUTED,
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
    drawRow(context, TerminalSetting.RADIUS, originX, originY, false);
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
        VanillaPalette.LABEL);

    drawRow(
        context,
        TerminalSetting.ACCESS,
        originX,
        originY,
        hovered == TerminalSetting.ACCESS && view.mayAdminister());
    labelled(
        context,
        textRenderer,
        TerminalScreenText.of(TerminalSetting.ACCESS.label()),
        TerminalScreenText.of("access." + view.accessMode().id()),
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.ACCESS.row()),
        view.mayAdminister());

    drawRow(context, TerminalSetting.TRUSTED, originX, originY, false);
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

    drawRow(context, TerminalSetting.CAPACITY, originX, originY, false);
    capacity(
        context,
        textRenderer,
        view,
        originX,
        originY + TerminalSettingsLayout.rowY(TerminalSetting.CAPACITY.row()));
  }

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
        VanillaPalette.MUTED,
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
            ? VanillaPalette.WARNING
            : VanillaPalette.LABEL,
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
          VanillaPalette.MUTED);
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
    TerminalChrome.drawActiveRow(
        context,
        entry.role() == ContainerRole.EXCLUDED,
        hovered,
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
        changeable ? VanillaPalette.LABEL : VanillaPalette.MUTED,
        false);
    final Text chip = ContainerRowText.role(entry.role());
    context.drawText(
        textRenderer,
        chip,
        left + TerminalSettingsLayout.ROW_WIDTH - CHIP_WIDTH,
        textY,
        changeable ? ContainerRowText.roleColour(entry.role()) : VanillaPalette.MUTED,
        false);
  }

  private static void drawRow(
      DrawContext context, TerminalSetting setting, int originX, int originY, boolean hovered) {
    TerminalChrome.drawRow(
        context,
        hovered,
        originX + TerminalSettingsLayout.PANEL_X,
        originY + TerminalSettingsLayout.rowY(setting.row()),
        TerminalSettingsLayout.FULL_ROW_WIDTH,
        TerminalSettingsLayout.ROW_HEIGHT - 1);
  }

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
    context.drawText(textRenderer, name, left + TEXT_INSET, textY, VanillaPalette.MUTED, false);
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
        changeable ? VanillaPalette.LABEL : VanillaPalette.MUTED,
        false);
  }

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

  private static void stepper(
      DrawContext context,
      TextRenderer textRenderer,
      String glyph,
      int x,
      int y,
      boolean enabled,
      boolean hovered) {
    final int size = SettingsControl.size();
    TerminalChrome.controlOrUnavailable(enabled, hovered).draw(context, x, y, size, size);
    context.drawText(
        textRenderer,
        glyph,
        x + (size - textRenderer.getWidth(glyph)) / 2,
        y + (size - LINE_HEIGHT) / 2 + 1,
        enabled ? VanillaPalette.LABEL : VanillaPalette.MUTED,
        false);
  }

  private void drawScrollbar(DrawContext context, int originX, int originY) {
    TerminalChrome.drawScrollbar(
        context,
        scrollbar,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalSettingsLayout.CONTENT_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalSettingsLayout.CONTENT_HEIGHT);
  }
}
