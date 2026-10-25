package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterPrompt {
  private final TagPromptList list = new TagPromptList();

  private BlockPos container;

  public void open(BlockPos edited) {
    container = edited == null ? null : edited.toImmutable();
    list.reset();
  }

  public void close() {
    container = null;
    list.releaseScrollbar();
  }

  public boolean isOpen() {
    return container != null;
  }

  public BlockPos container() {
    return container;
  }

  public TagPromptList list() {
    return list;
  }

  public void fit(NetworkTagView view) {
    list.fit(view.tags().size());
  }

  public ContainerRoleEntry rowIn(NetworkSettingsView view) {
    if (!isOpen()) {
      return null;
    }
    for (final ContainerRoleEntry entry : view.containers()) {
      if (entry.container().equals(container)) {
        return entry;
      }
    }
    return null;
  }

  public NetworkTagEntry tagAt(NetworkTagView view, int windowX, int windowY) {
    final int index = list.indexAt(windowX, windowY);
    final List<NetworkTagEntry> tags = view.tags();
    return index < 0 || index >= tags.size() ? null : tags.get(index);
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView settings,
      NetworkTagView tags,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final ContainerRoleEntry row = rowIn(settings);
    if (row == null) {
      return;
    }
    TagPromptFrame.draw(
        context,
        textRenderer,
        ContainerBlocks.at(row.container()),
        TerminalScreenText.of(
            "settings.container.filter.title",
            ContainerRowText.title(row, ContainerBlocks.at(row.container()))),
        list,
        HintLine.of(HintLine.key("esc"), HintLine.words("hint.done")),
        tags.tags().isEmpty() ? null : footerFor(row, tags),
        tags.tags().isEmpty() ? TerminalScreenText.of("settings.container.filter.notags") : null,
        originX,
        originY,
        () -> drawRows(context, textRenderer, row, tags, originX, originY, mouseX, mouseY));
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      ContainerRoleEntry row,
      NetworkTagView tags,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> all = tags.tags();
    final NetworkTagEntry hovered = tagAt(tags, mouseX, mouseY);
    for (int visible = 0; visible < TagPromptLayout.VISIBLE_ROWS; visible++) {
      final int index = list.indexOfRow(visible);
      if (index >= all.size()) {
        return;
      }
      final NetworkTagEntry tag = all.get(index);
      TagCheckRow.draw(
          context,
          textRenderer,
          FilterColumnText.nameOf(tag),
          row.accepts(tag.id()),
          tag.equals(hovered),
          originX + TagPromptLayout.LIST.left(),
          originY + TagPromptLayout.LIST.rowY(visible));
    }
  }

  private static Text footerFor(ContainerRoleEntry row, NetworkTagView tags) {
    if (tags.tags().isEmpty()) {
      return TerminalScreenText.of("settings.container.filter.notags");
    }
    return row.acceptsNothing()
        ? TerminalScreenText.of("settings.container.filter.empty.short")
        : TerminalScreenText.of(
            "settings.container.filter.count", row.acceptedTags().size(), tags.tags().size());
  }
}
