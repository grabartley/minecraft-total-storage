package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Draws how a network is configured, and says which row a click landed on.
 *
 * <p>Everything shown comes from the server's answer rather than from anything worked out here. The
 * client does not know the server's radius cap, cannot turn a trust list into names, and never sees
 * the block entity the roles live on, so a panel that computed any of it would be a panel that
 * disagrees with the terminal it is describing.
 *
 * <p>Controls a player may not use are drawn flat rather than hidden. A missing button reads as a
 * broken screen; a disabled one reads as somebody else's terminal, which is the truth.
 */
public final class TerminalSettingsPanel {
  private static final Identifier ROW = TerminalSprites.of("filter/row");
  private static final Identifier ROW_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier ROW_ACTIVE = TerminalSprites.of("filter/row_active");
  private static final Identifier CONTROL = TerminalSprites.of("button/normal");
  private static final Identifier CONTROL_HOVER = TerminalSprites.of("button/hover");
  private static final Identifier CONTROL_DISABLED = TerminalSprites.of("button/disabled");

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int MUTED_COLOUR = 0x9AA3B3;
  private static final int TEXT_INSET = 2;

  /** Where a row's role chip starts, measured from the right edge of the panel. */
  private static final int CHIP_WIDTH = 46;

  private final ListingScroll scroll = new ListingScroll(1, TerminalScreenLayout.CONTAINER_ROWS);

  public ListingScroll scroll() {
    return scroll;
  }

  /** Keeps the scroll inside a list that may have changed size since the last answer. */
  public void fit(NetworkSettingsView view) {
    scroll.fit(view.containerTotal());
  }

  /** The row of the whole container list a screen position is over, or -1 for none. */
  public int rowAt(int windowX, int windowY) {
    final int row = TerminalScreenLayout.containerRowAt(windowX, windowY);
    return row == TerminalScreenLayout.NO_CELL ? -1 : scroll.firstIndex() + row;
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int hoveredRow,
      SettingsControl hoveredControl) {
    drawHeader(context, textRenderer, view, originX, originY, hoveredControl);
    drawContainers(context, textRenderer, view, originX, originY, hoveredRow);
  }

  private void drawHeader(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      SettingsControl hovered) {
    label(
        context,
        textRenderer,
        view.isClamped()
            ? TerminalScreenText.of(
                "settings.radius.clamped", view.radius(), view.effectiveRadius())
            : TerminalScreenText.of("settings.radius", view.radius(), view.maxRadius()),
        originX,
        originY + SettingsControl.rowY(SettingsControl.radiusRow()),
        LABEL_COLOUR);
    control(
        context,
        textRenderer,
        "-",
        originX + SettingsControl.radiusDownX(),
        originY + SettingsControl.rowY(SettingsControl.radiusRow()),
        view.mayAdminister(),
        hovered == SettingsControl.RADIUS_DOWN);
    control(
        context,
        textRenderer,
        "+",
        originX + SettingsControl.radiusUpX(),
        originY + SettingsControl.rowY(SettingsControl.radiusRow()),
        view.mayAdminister(),
        hovered == SettingsControl.RADIUS_UP);

    label(
        context,
        textRenderer,
        TerminalScreenText.of(
            "settings.access", TerminalScreenText.of("access." + view.accessMode().id())),
        originX,
        originY + SettingsControl.rowY(SettingsControl.accessRow()),
        LABEL_COLOUR);
    control(
        context,
        textRenderer,
        ">",
        originX + SettingsControl.accessCycleX(),
        originY + SettingsControl.rowY(SettingsControl.accessRow()),
        view.mayAdminister(),
        hovered == SettingsControl.ACCESS_CYCLE);

    label(
        context,
        textRenderer,
        view.trusted().isEmpty()
            ? TerminalScreenText.of("settings.trusted.none")
            : TerminalScreenText.of("settings.trusted", String.join(", ", view.trusted())),
        originX,
        originY + TerminalScreenLayout.settingsRowY(2),
        MUTED_COLOUR);
    label(
        context,
        textRenderer,
        TerminalScreenText.of(
            "settings.tally",
            view.tally().total(),
            view.tally().general(),
            view.tally().filtered(),
            view.tally().excluded()),
        originX,
        originY + TerminalScreenLayout.settingsRowY(3),
        MUTED_COLOUR);
  }

  private void drawContainers(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int hoveredRow) {
    if (!view.hasContainers()) {
      label(
          context,
          textRenderer,
          TerminalScreenText.of(view.powered() ? "settings.empty" : "settings.offline"),
          originX,
          originY + TerminalScreenLayout.CONTAINER_LIST_Y + 2,
          MUTED_COLOUR);
      return;
    }
    for (int row = 0; row < TerminalScreenLayout.CONTAINER_ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      final ContainerRoleEntry entry = view.at(index);
      if (entry == null) {
        continue;
      }
      drawContainerRow(
          context,
          textRenderer,
          entry,
          originX,
          originY,
          row,
          index == hoveredRow && view.mayAdminister(),
          view.mayAdminister());
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
    final int left = originX + TerminalScreenLayout.SETTINGS_X;
    final int top = originY + TerminalScreenLayout.containerRowY(row);
    final int width = TerminalScreenLayout.SETTINGS_WIDTH;
    context.drawGuiTexture(
        entry.role() == ContainerRole.EXCLUDED ? ROW_ACTIVE : hovered ? ROW_HOVER : ROW,
        left,
        top,
        width,
        TerminalScreenLayout.CONTAINER_ROW_HEIGHT - 1);
    context.drawText(
        textRenderer,
        ContainerRowText.position(entry),
        left + TEXT_INSET + 1,
        top + 2,
        changeable ? LABEL_COLOUR : MUTED_COLOUR,
        false);
    final Text chip = ContainerRowText.role(entry.role());
    context.drawText(
        textRenderer,
        chip,
        left + width - CHIP_WIDTH,
        top + 2,
        changeable ? ContainerRowText.roleColour(entry.role()) : MUTED_COLOUR,
        false);
  }

  private static void label(
      DrawContext context, TextRenderer textRenderer, Text text, int originX, int y, int colour) {
    context.drawText(
        textRenderer,
        text,
        originX + TerminalScreenLayout.SETTINGS_X + TEXT_INSET,
        y + 2,
        colour,
        false);
  }

  /**
   * A small square button carrying one glyph. Drawn rather than added as a widget so it tracks the
   * panel it belongs to, and so it disappears with the tab rather than needing to be told to.
   */
  private static void control(
      DrawContext context,
      TextRenderer textRenderer,
      String glyph,
      int x,
      int y,
      boolean enabled,
      boolean hovered) {
    final int size = TerminalScreenLayout.SETTINGS_CONTROL_SIZE;
    context.drawGuiTexture(
        !enabled ? CONTROL_DISABLED : hovered ? CONTROL_HOVER : CONTROL, x, y, size, size);
    context.drawText(
        textRenderer,
        glyph,
        x + (size - textRenderer.getWidth(glyph)) / 2,
        y + 1,
        enabled ? LABEL_COLOUR : MUTED_COLOUR,
        false);
  }
}
