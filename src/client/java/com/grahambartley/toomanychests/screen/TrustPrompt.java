package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.access.TrustEditOutcome;
import com.grahambartley.toomanychests.network.TerminalTrustReplyPayload;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TrustPrompt {
  public static final int REMOVE_WIDTH = 40;

  private static final int FIELD_HEIGHT = 12;

  private final TagPromptList list = new TagPromptList();

  private PlayerNameEntry entry;
  private TrustEditOutcome refusal;
  private String refusedName = "";

  public void open() {
    entry = new PlayerNameEntry();
    forgetRefusal();
    list.reset();
  }

  public void close() {
    entry = null;
    forgetRefusal();
    list.releaseScrollbar();
  }

  public boolean isOpen() {
    return entry != null;
  }

  public TagPromptList list() {
    return list;
  }

  public String name() {
    return entry == null ? "" : entry.text();
  }

  public TrustEditOutcome refusal() {
    return refusal;
  }

  public void fit(NetworkSettingsView view) {
    list.fit(view.trusted().size());
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    if (!isOpen()) {
      return PromptKey.HANDLED;
    }
    final String before = entry.text();
    return forgetRefusalOnEdit(before, entry.keyPressed(keyCode, selectAllHeld));
  }

  public PromptKey charTyped(char typed) {
    if (!isOpen()) {
      return PromptKey.HANDLED;
    }
    final String before = entry.text();
    return forgetRefusalOnEdit(before, entry.charTyped(typed));
  }

  private PromptKey forgetRefusalOnEdit(String before, PromptKey outcome) {
    if (!before.equals(entry.text())) {
      forgetRefusal();
    }
    return outcome;
  }

  public void answered(TerminalTrustReplyPayload reply) {
    if (!isOpen() || reply == null) {
      return;
    }
    if (reply.outcome() == TrustEditOutcome.DENIED) {
      close();
      return;
    }
    if (reply.outcome() == TrustEditOutcome.ADDED && reply.player().equalsIgnoreCase(name())) {
      entry.clear();
      forgetRefusal();
      return;
    }
    if (reply.outcome().keepsTheTypedName()) {
      refusal = reply.outcome();
      refusedName = reply.player();
    }
  }

  public String removalAt(NetworkSettingsView view, int windowX, int windowY) {
    if (!isOverRemove(windowX)) {
      return null;
    }
    final int index = list.indexAt(windowX, windowY);
    final List<String> trusted = view.trusted();
    return index < 0 || index >= trusted.size() ? null : trusted.get(index);
  }

  public static boolean isOverRemove(int windowX) {
    final int rowRight = TagPromptLayout.textLeft() + TagPromptLayout.ROW_WIDTH;
    return windowX >= rowRight - REMOVE_WIDTH && windowX < rowRight;
  }

  private void forgetRefusal() {
    refusal = null;
    refusedName = "";
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView view,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    if (!isOpen()) {
      return;
    }
    TagPromptFrame.draw(
        context,
        textRenderer,
        TerminalScreenText.of("settings.trusted.title"),
        list,
        originX,
        originY,
        () -> drawRows(context, textRenderer, view.trusted(), originX, originY, mouseX, mouseY),
        () -> drawEntry(context, textRenderer, originX, originY));
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      List<String> trusted,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final int left = originX + TagPromptLayout.textLeft();
    if (trusted.isEmpty()) {
      context.drawText(
          textRenderer,
          TerminalScreenText.of("settings.trusted.none"),
          left + TagPromptLayout.TEXT_MARGIN,
          originY + textY(0),
          VanillaPalette.MUTED,
          false);
      return;
    }
    final int hovered = list.indexAt(mouseX, mouseY);
    for (int visible = 0; visible < TagPromptLayout.VISIBLE_ROWS; visible++) {
      final int index = list.indexOfRow(visible);
      if (index >= trusted.size()) {
        return;
      }
      final boolean overRemove = index == hovered && isOverRemove(mouseX);
      TerminalChrome.drawRow(
          context,
          index == hovered,
          left,
          originY + TagPromptLayout.rowY(visible),
          TagPromptLayout.ROW_WIDTH,
          TagPromptLayout.ROW_HEIGHT);
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(
              textRenderer,
              Text.literal(trusted.get(index)),
              TagPromptLayout.ROW_WIDTH - REMOVE_WIDTH - 2 * TagPromptLayout.TEXT_MARGIN),
          left + TagPromptLayout.TEXT_MARGIN,
          originY + textY(visible),
          VanillaPalette.LABEL,
          false);
      final Text remove = TerminalScreenText.of("settings.trusted.remove");
      context.drawText(
          textRenderer,
          remove,
          left
              + TagPromptLayout.ROW_WIDTH
              - TagPromptLayout.TEXT_MARGIN
              - textRenderer.getWidth(remove),
          originY + textY(visible),
          overRemove ? VanillaPalette.ACCENT : VanillaPalette.MUTED,
          false);
    }
  }

  private static int textY(int visibleRow) {
    return TagPromptLayout.rowY(visibleRow)
        + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.LINE_HEIGHT) / 2
        + 1;
  }

  private void drawEntry(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    final int left = originX + TagPromptLayout.textLeft();
    final int fieldTop = originY + TagPromptLayout.footerTop() - TagPromptLayout.GAP;
    TerminalSprite.FIELD_FOCUSED.draw(
        context, left, fieldTop, TagPromptLayout.TEXT_WIDTH, FIELD_HEIGHT);
    context.drawText(
        textRenderer, name(), left + 2, fieldTop + 2, VanillaPalette.FIELD_TEXT, false);
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, hint(), TagPromptLayout.TEXT_WIDTH),
        left,
        fieldTop + FIELD_HEIGHT + 2,
        refusal == null ? VanillaPalette.MUTED : VanillaPalette.WARNING,
        false);
  }

  private Text hint() {
    return refusal == null
        ? TerminalScreenText.of("settings.trusted.hint")
        : TrustPromptText.refusal(refusal, refusedName);
  }
}
