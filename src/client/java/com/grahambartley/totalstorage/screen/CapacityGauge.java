package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientConfigMirror;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CapacityGauge {
  public static final int BAR_HEIGHT = 8;

  public static final int LINE_GAP = 3;

  private CapacityGauge() {}

  public static boolean isNearlyFull(NetworkSettingsView view) {
    return view.answered()
        && !view.capacity().isUnknown()
        && view.capacity()
            .isNearCapacity(ClientConfigMirror.current().nearCapacityWarningPercent());
  }

  public static int filledWidth(NetworkCapacity room, int innerWidth) {
    if (room.isUnknown() || room.totalSlots() <= 0) {
      return 0;
    }
    final long used = Math.max(0, Math.min(room.totalSlots(), room.usedSlots()));
    final int filled = (int) ((used * innerWidth + room.totalSlots() / 2) / room.totalSlots());
    return used > 0 ? Math.max(1, filled) : 0;
  }

  public static Text line(NetworkSettingsView view) {
    if (!view.answered()) {
      return TerminalScreenText.of("settings.pending");
    }
    final NetworkCapacity room = view.capacity();
    if (room.isUnknown()) {
      return TerminalScreenText.of("capacity.none");
    }
    if (isNearlyFull(view)) {
      return TerminalScreenText.of("capacity.nearly_full");
    }
    return room.freeSlots() == 1
        ? TerminalScreenText.of("capacity.free.one")
        : TerminalScreenText.of("capacity.free", ItemCountFormat.exact(room.freeSlots()));
  }

  public static Text detail(NetworkSettingsView view) {
    final NetworkCapacity room = view.capacity();
    return view.answered() && !room.isUnknown()
        ? TerminalScreenText.of(
            "settings.capacity.slots",
            ItemCountFormat.exact(room.freeSlots()),
            ItemCountFormat.exact(room.totalSlots()))
        : null;
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int x,
      int y,
      int width) {
    TerminalChrome.fill(context, x, y, width, BAR_HEIGHT, VanillaPalette.SUNKEN_SHADOW);
    TerminalChrome.fill(context, x, y, width - 1, 1, VanillaPalette.OUTLINE);
    TerminalChrome.fill(context, x, y + 1, 1, BAR_HEIGHT - 2, VanillaPalette.OUTLINE);
    TerminalChrome.fill(
        context, x + 1, y + BAR_HEIGHT - 1, width - 1, 1, VanillaPalette.BEVEL_HIGHLIGHT);
    TerminalChrome.fill(
        context, x + width - 1, y, 1, BAR_HEIGHT - 1, VanillaPalette.BEVEL_HIGHLIGHT);
    final boolean warn = isNearlyFull(view);
    if (view.answered()) {
      TerminalChrome.fill(
          context,
          x + 1,
          y + 1,
          filledWidth(view.capacity(), width - 2),
          BAR_HEIGHT - 2,
          warn ? VanillaPalette.ACCENT : VanillaPalette.BEVEL_HIGHLIGHT);
    }
    final int lineY = y + BAR_HEIGHT + LINE_GAP;
    int textX = x;
    if (warn) {
      WarningGlyph.draw(context, x, lineY, VanillaPalette.ACCENT);
      textX += WarningGlyph.WIDTH + WarningGlyph.GAP;
    }
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, line(view), x + width - textX),
        textX,
        lineY,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }

  public static boolean isOver(int windowX, int windowY, int x, int y, int width) {
    return windowX >= x
        && windowX < x + width
        && windowY >= y
        && windowY < y + BAR_HEIGHT + LINE_GAP + 9;
  }
}
