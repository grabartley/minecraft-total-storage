package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import com.grahambartley.toomanychests.text.CountedNoun;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterPrompt {
  private static final float ELEVATION = 300.0F;

  private final ListingScroll scroll = new ListingScroll(1, TagPromptLayout.VISIBLE_ROWS);

  private BlockPos container;

  public void open(BlockPos edited) {
    container = edited == null ? null : edited.toImmutable();
    scroll.scrollTo(0);
  }

  public void close() {
    container = null;
  }

  public boolean isOpen() {
    return container != null;
  }

  public BlockPos container() {
    return container;
  }

  public void fit(NetworkTagView view) {
    scroll.fit(view.tags().size());
  }

  public boolean scrollBy(int notches) {
    return scroll.scrollBy(notches);
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
    final int row = TagPromptLayout.rowAt(windowX, windowY);
    if (row == TagPromptLayout.NO_ROW) {
      return null;
    }
    final List<NetworkTagEntry> tags = view.tags();
    final int index = scroll.firstIndex() + row;
    return index >= tags.size() ? null : tags.get(index);
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
    final int left = originX + TagPromptLayout.left();
    final int top = originY + TagPromptLayout.top();
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(context, left, top, TagPromptLayout.WIDTH, TagPromptLayout.HEIGHT);
    drawLine(
        context,
        textRenderer,
        TerminalScreenText.of("settings.container.filter.title", ContainerRowText.label(row)),
        left,
        top + TagPromptLayout.PADDING,
        VanillaPalette.LABEL);
    drawRows(context, textRenderer, row, tags, originX, originY, mouseX, mouseY);
    drawLine(
        context,
        textRenderer,
        footerFor(row, tags),
        left,
        top + TagPromptLayout.HEIGHT - TagPromptLayout.PADDING - TagPromptLayout.LINE_HEIGHT,
        VanillaPalette.MUTED);
    context.getMatrices().pop();
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
      final int index = scroll.firstIndex() + visible;
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
          originX + TagPromptLayout.left() + TagPromptLayout.PADDING,
          originY + TagPromptLayout.rowY(visible));
    }
  }

  private static Text footerFor(ContainerRoleEntry row, NetworkTagView tags) {
    if (tags.tags().isEmpty()) {
      return TerminalScreenText.of("settings.container.filter.notags");
    }
    return row.acceptsNothing()
        ? TerminalScreenText.of("settings.container.filter.empty")
        : TerminalScreenText.of(
            "settings.container.filter.count",
            row.acceptedTags().size(),
            CountedNoun.TAGS.of(tags.tags().size()));
  }

  private static void drawLine(
      DrawContext context, TextRenderer textRenderer, Text line, int left, int top, int colour) {
    final OrderedText trimmed = TrimmedText.toWidth(textRenderer, line, TagPromptLayout.ROW_WIDTH);
    context.drawText(textRenderer, trimmed, left + TagPromptLayout.PADDING, top, colour, false);
  }
}
