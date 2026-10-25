package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ContainerListView {
  public static final int SLOT_GAP = 4;

  public static final int ROLE_WIDTH = 64;

  public static final int ROLE_HEIGHT = 16;

  public static final int ROLE_X =
      TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH - ROLE_WIDTH;

  public static final int TEXT_X =
      TerminalSettingsLayout.LIST_X + TerminalScreenLayout.SLOT_SIZE + SLOT_GAP;

  public static final int TEXT_WIDTH = ROLE_X - 6 - TEXT_X;

  private static final int LINE_HEIGHT = 9;

  private ContainerListView() {}

  public static int roleY(int row) {
    return TerminalSettingsLayout.rowY(row) + (TerminalSettingsLayout.ROW_HEIGHT - ROLE_HEIGHT) / 2;
  }

  public static int slotY(int row) {
    return TerminalSettingsLayout.rowY(row)
        + (TerminalSettingsLayout.ROW_HEIGHT - TerminalScreenLayout.SLOT_SIZE) / 2;
  }

  public static void drawBackground(
      DrawContext context, NetworkSettingsView view, int firstIndex, int originX, int originY) {
    for (int row = 0; row < TerminalSettingsLayout.ROWS; row++) {
      if (view.at(firstIndex + row) == null) {
        return;
      }
      VanillaSprite.SLOT.draw(
          context,
          originX + TerminalSettingsLayout.LIST_X,
          originY + slotY(row),
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int firstIndex,
      int hoveredIndex) {
    if (!view.hasContainers()) {
      drawEmpty(context, textRenderer, view);
      return;
    }
    for (int row = 0; row < TerminalSettingsLayout.ROWS; row++) {
      final int index = firstIndex + row;
      final ContainerRoleEntry entry = view.at(index);
      if (entry == null) {
        return;
      }
      drawRow(context, textRenderer, entry, row, index == hoveredIndex, view.mayAdminister());
    }
  }

  private static void drawEmpty(
      DrawContext context, TextRenderer textRenderer, NetworkSettingsView view) {
    final Text message = TerminalScreenText.of(emptyKey(view));
    final List<OrderedText> lines =
        textRenderer.wrapLines(message, TerminalSettingsLayout.LIST_WIDTH - 8);
    int top =
        TerminalSettingsLayout.LIST_Y
            + (TerminalSettingsLayout.LIST_HEIGHT - lines.size() * LINE_HEIGHT) / 2;
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

  private static String emptyKey(NetworkSettingsView view) {
    if (!view.answered()) {
      return "settings.pending";
    }
    return view.powered() ? "settings.empty" : "settings.offline";
  }

  private static void drawRow(
      DrawContext context,
      TextRenderer textRenderer,
      ContainerRoleEntry entry,
      int row,
      boolean hovered,
      boolean changeable) {
    final int top = TerminalSettingsLayout.rowY(row);
    final ItemStack block = ContainerBlocks.at(entry.container());
    final ItemStack icon =
        block.isEmpty() ? entry.icon().map(ItemVariant::toStack).orElse(ItemStack.EMPTY) : block;
    context.drawItem(
        icon,
        TerminalSettingsLayout.LIST_X + TerminalScreenLayout.SLOT_ITEM_OFFSET,
        slotY(row) + TerminalScreenLayout.SLOT_ITEM_OFFSET);
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, ContainerRowText.title(entry, block), TEXT_WIDTH),
        TEXT_X,
        top + 2,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, ContainerRowText.detail(entry), TEXT_WIDTH),
        TEXT_X,
        top + 11,
        VanillaPalette.opaque(VanillaPalette.MUTED),
        false);
    ButtonFace.draw(
        context,
        textRenderer,
        ContainerRowText.roleChip(entry),
        ROLE_X,
        roleY(row),
        ROLE_WIDTH,
        ROLE_HEIGHT,
        changeable,
        hovered && changeable,
        false,
        roleTextColour(entry, changeable));
  }

  public static int roleTextColour(ContainerRoleEntry entry, boolean changeable) {
    if (!changeable) {
      return VanillaPalette.BUTTON_TEXT_DISABLED;
    }
    return entry.acceptsNothing() ? VanillaPalette.WARNING : VanillaPalette.BUTTON_TEXT;
  }

  public static List<Text> tooltip(ContainerRoleEntry entry, NetworkTagView tags) {
    final List<Text> lines = new ArrayList<>();
    lines.add(ContainerRowText.title(entry, ContainerBlocks.at(entry.container())));
    lines.add(ContainerRowText.position(entry).copy().formatted(Formatting.GRAY));
    if (entry.totalSlots() > 0) {
      lines.add(ContainerRowText.slotsUsed(entry).formatted(Formatting.GRAY));
    }
    if (!entry.isEmpty()) {
      lines.add(ContainerRowText.itemTotal(entry).formatted(Formatting.GRAY));
    }
    lines.add(ContainerRowText.roleChip(entry));
    lines.add(ContainerRowText.roleMeaning(entry.role()).formatted(Formatting.GRAY));
    if (entry.isFiltered()) {
      lines.add(ContainerRowText.acceptedTags(entry, tags).formatted(Formatting.GRAY));
    }
    return lines;
  }

  public static List<HintLine> hints(ContainerRoleEntry entry, boolean mayAdminister) {
    if (!mayAdminister) {
      return List.of();
    }
    final List<HintLine> hints = new ArrayList<>();
    hints.add(
        HintLine.of(
            HintLine.key("click"),
            HintLine.words("hint.next_role"),
            HintLine.key("right_click"),
            HintLine.words("hint.previous_role")));
    if (entry.isFiltered()) {
      hints.add(
          HintLine.of(
              HintLine.key("shift"),
              HintLine.words(Text.literal("+")),
              HintLine.key("click"),
              HintLine.words("hint.choose_tags")));
    }
    hints.add(
        HintLine.of(
            HintLine.key("shift"),
            HintLine.words(Text.literal("+")),
            HintLine.key("right_click"),
            HintLine.words("hint.rename")));
    return hints;
  }
}
