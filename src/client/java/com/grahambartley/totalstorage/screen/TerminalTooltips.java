package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingRow;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class TerminalTooltips {
  private TerminalTooltips() {}

  public static void text(
      DrawContext context, TextRenderer textRenderer, Text line, int mouseX, int mouseY) {
    if (line != null) {
      HintTooltip.draw(context, textRenderer, List.of(line), List.of(), mouseX, mouseY);
    }
  }

  public static void tab(
      DrawContext context, TextRenderer textRenderer, TerminalTab hovered, int mouseX, int mouseY) {
    if (hovered != null) {
      text(context, textRenderer, TerminalScreenText.of(hovered.label()), mouseX, mouseY);
    }
  }

  public static List<HintLine> cellHints(boolean mayTag, boolean onMac) {
    final List<HintLine> hints = new ArrayList<>();
    if (mayTag) {
      hints.add(
          HintLine.of(HintLine.key(onMac ? "cmd" : "ctrl"), HintLine.words("hint.click_to_tag")));
    }
    hints.add(HintLine.of(HintLine.key("middle"), HintLine.words("hint.click_for_amount")));
    return hints;
  }

  public static void cell(
      DrawContext context,
      TextRenderer textRenderer,
      MinecraftClient client,
      ListingRow<ItemVariant> row,
      boolean mayTag,
      int mouseX,
      int mouseY) {
    if (row == null) {
      return;
    }
    final ItemStack stack = row.variant().toStack();
    final List<Text> lines = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
    lines.addAll(ListingTooltip.extraLines(row));
    HintTooltip.draw(
        context,
        textRenderer,
        lines,
        cellHints(mayTag, MinecraftClient.IS_SYSTEM_MAC),
        mouseX,
        mouseY);
  }

  public static void container(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      NetworkTagView tags,
      TerminalSettingsPanel settings,
      int windowX,
      int windowY,
      int mouseX,
      int mouseY) {
    final int index = settings.containerRowAt(windowX, windowY);
    final ContainerRoleEntry entry = index == TerminalSettingsLayout.NO_ROW ? null : view.at(index);
    if (entry == null) {
      return;
    }
    HintTooltip.draw(
        context,
        textRenderer,
        ContainerListView.tooltip(entry, tags),
        ContainerListView.hints(
            entry, view.mayAdminister(), ContainerListView.isOverRole(windowX, windowY)),
        mouseX,
        mouseY);
  }

  public static void filterTag(
      DrawContext context,
      TextRenderer textRenderer,
      TerminalFilterColumn column,
      int windowX,
      int windowY,
      int mouseX,
      int mouseY) {
    final List<Text> lines = column.tooltipAt(windowX, windowY);
    if (!lines.isEmpty()) {
      HintTooltip.draw(context, textRenderer, lines, List.of(), mouseX, mouseY);
    }
  }
}
