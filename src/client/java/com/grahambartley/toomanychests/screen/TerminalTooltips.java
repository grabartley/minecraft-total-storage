package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.screenhandler.FilterColumnLayout;
import com.grahambartley.toomanychests.screenhandler.TagsSectionLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import com.grahambartley.toomanychests.text.CountedNoun;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TerminalTooltips {
  private TerminalTooltips() {}

  public static void tab(
      DrawContext context, TextRenderer textRenderer, TerminalTab hovered, int mouseX, int mouseY) {
    if (hovered != null) {
      WrappedTooltip.draw(
          context, textRenderer, TerminalScreenText.of(hovered.label()), mouseX, mouseY);
    }
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
    if (mayTag) {
      lines.add(TagText.assignHint(MinecraftClient.IS_SYSTEM_MAC).formatted(Formatting.DARK_GRAY));
    }
    WrappedTooltip.draw(context, textRenderer, lines, mouseX, mouseY);
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
    final List<Text> lines = new ArrayList<>();
    if (entry.isNamed()) {
      lines.add(Text.literal(entry.name()));
      lines.add(ContainerRowText.position(entry).copy().formatted(Formatting.GRAY));
    } else {
      lines.add(ContainerRowText.position(entry));
    }
    if (entry.totalSlots() > 0) {
      lines.add(ContainerRowText.slotsUsed(entry).formatted(Formatting.GRAY));
    }
    if (!entry.isEmpty()) {
      lines.add(ContainerRowText.itemTotal(entry).formatted(Formatting.GRAY));
    }
    lines.add(ContainerRowText.role(entry.role()));
    lines.add(ContainerRowText.roleMeaning(entry.role()).formatted(Formatting.GRAY));
    if (entry.isFiltered()) {
      lines.add(ContainerRowText.acceptedTags(entry, tags).formatted(Formatting.GRAY));
    }
    if (view.mayAdminister()) {
      lines.add(TerminalScreenText.of("settings.role.hint").formatted(Formatting.DARK_GRAY));
      lines.add(
          TerminalScreenText.of("settings.container.name.hint.row")
              .formatted(Formatting.DARK_GRAY));
      if (entry.isFiltered()) {
        lines.add(
            TerminalScreenText.of("settings.container.filter.hint")
                .formatted(Formatting.DARK_GRAY));
      }
    }
    WrappedTooltip.draw(context, textRenderer, lines, mouseX, mouseY);
  }

  public static void copyTags(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      SettingsSection section,
      int windowX,
      int windowY,
      int mouseX,
      int mouseY) {
    if (section != SettingsSection.TAGS
        || !view.mayAdminister()
        || !TagsSectionLayout.isOverCopy(windowX, windowY)) {
      return;
    }
    WrappedTooltip.draw(context, textRenderer, TerminalScreenText.of("tags.copy"), mouseX, mouseY);
  }

  public static void filterTag(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      TerminalFilterColumn column,
      int windowX,
      int windowY,
      int mouseX,
      int mouseY) {
    if (!FilterColumnLayout.isOverColumn(windowX, windowY)) {
      return;
    }
    final NetworkTagEntry tag = column.tagAt(view, windowX, windowY);
    if (tag == null) {
      return;
    }
    final List<Text> lines = new ArrayList<>();
    lines.add(FilterColumnText.nameOf(tag));
    lines.add(
        TerminalScreenText.of(
                "filter.tag.matching",
                CountedNoun.ITEMS.ofFormatted(FilterColumnText.count(tag), tag.matchingItems()))
            .formatted(Formatting.GRAY));
    WrappedTooltip.draw(context, textRenderer, lines, mouseX, mouseY);
  }
}
