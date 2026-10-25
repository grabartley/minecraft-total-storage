package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.access.TrustEditOutcome;
import com.grahambartley.totalstorage.network.TerminalTrustReplyPayload;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TrustPrompt {
  public static final int REMOVE_WIDTH = 44;

  public static final int REMOVE_HEIGHT = 12;

  public static final int ROW_HEIGHT = 16;

  public static final int VISIBLE_ROWS = 3;

  public static final int LINE_HEIGHT = 9;

  public static final int LIST_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT + 2;

  public static final int HEAD_SIZE = 8;

  public static final PromptFrame FRAME =
      PromptFrame.centred(
          LINE_HEIGHT
              + 4
              + LIST_HEIGHT
              + 5
              + PromptField.HEIGHT
              + TextPromptPanel.REFUSAL_GAP
              + LINE_HEIGHT
              + TextPromptPanel.KEYS_GAP
              + KeyCaps.HEIGHT);

  public static final int LIST_TOP = FRAME.contentTop() + LINE_HEIGHT + 4;

  public static final PromptListGeometry LIST =
      new PromptListGeometry(
          FRAME.contentLeft() + 1,
          LIST_TOP + 1,
          PromptFrame.CONTENT_WIDTH - 2 - 2 - 5,
          ROW_HEIGHT,
          VISIBLE_ROWS,
          FRAME.contentLeft() + PromptFrame.CONTENT_WIDTH - 1 - 5,
          5);

  public static final int FIELD_TOP = LIST_TOP + LIST_HEIGHT + 5;

  public static final int REFUSAL_TOP =
      FIELD_TOP + PromptField.HEIGHT + TextPromptPanel.REFUSAL_GAP;

  public static final int KEYS_TOP = REFUSAL_TOP + LINE_HEIGHT + TextPromptPanel.KEYS_GAP;

  private final TagPromptList list = new TagPromptList(LIST);

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
    if (!reply.outcome().changedTheList()) {
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

  public static int removeX() {
    return LIST.left() + LIST.rowWidth() - 2 - REMOVE_WIDTH;
  }

  public static boolean isOverRemove(int windowX) {
    return windowX >= removeX() && windowX < removeX() + REMOVE_WIDTH;
  }

  public static boolean isInside(int windowX, int windowY) {
    return FRAME.isInside(windowX, windowY);
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
    final int left = originX + FRAME.contentLeft();
    PromptFrame.begin(context);
    FRAME.drawPanel(context, originX, originY);
    PromptHeader.drawTitle(
        context,
        textRenderer,
        TerminalScreenText.of("settings.trusted.title"),
        left,
        originY + FRAME.contentTop(),
        PromptFrame.CONTENT_WIDTH);
    TerminalChrome.drawInset(
        context, left, originY + LIST_TOP, PromptFrame.CONTENT_WIDTH, LIST_HEIGHT);
    drawRows(context, textRenderer, view.trusted(), originX, originY, mouseX, mouseY);
    list.drawScrollbar(context, originX, originY);
    PromptField.draw(
        context,
        textRenderer,
        left,
        originY + FIELD_TOP,
        PromptFrame.CONTENT_WIDTH,
        name(),
        TerminalScreenText.of("settings.trusted.placeholder"),
        null,
        refusal != null);
    if (refusal != null) {
      PromptField.drawRefusal(
          context,
          textRenderer,
          TrustPromptText.refusal(refusal, refusedName),
          left,
          originY + REFUSAL_TOP,
          PromptFrame.CONTENT_WIDTH);
    }
    KeyCaps.draw(
        context,
        textRenderer,
        HintLine.of(
            HintLine.key("enter"),
            HintLine.words("hint.add"),
            HintLine.key("esc"),
            HintLine.words("hint.close")),
        left,
        originY + KEYS_TOP,
        VanillaPalette.MUTED,
        false);
    PromptFrame.end(context);
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      List<String> trusted,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final int left = originX + LIST.left();
    if (trusted.isEmpty()) {
      context.drawText(
          textRenderer,
          TerminalScreenText.of("settings.trusted.none"),
          left + 3,
          originY + LIST.rowY(0) + (ROW_HEIGHT - 8) / 2,
          VanillaPalette.opaque(VanillaPalette.MUTED),
          false);
      return;
    }
    final int hovered = list.indexAt(mouseX, mouseY);
    for (int visible = 0; visible < VISIBLE_ROWS; visible++) {
      final int index = list.indexOfRow(visible);
      if (index >= trusted.size()) {
        return;
      }
      final int top = originY + LIST.rowY(visible);
      final String player = trusted.get(index);
      PlayerHeads.draw(context, player, left + 3, top + (ROW_HEIGHT - HEAD_SIZE) / 2, HEAD_SIZE);
      final int nameLeft = left + 3 + HEAD_SIZE + 4;
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(
              textRenderer, Text.literal(player), originX + removeX() - 6 - nameLeft),
          nameLeft,
          top + (ROW_HEIGHT - 8) / 2,
          VanillaPalette.opaque(VanillaPalette.LABEL),
          false);
      final boolean overRemove = index == hovered && isOverRemove(mouseX);
      final int removeTop = top + (ROW_HEIGHT - REMOVE_HEIGHT) / 2;
      VanillaSprite.button(true, overRemove)
          .draw(context, originX + removeX(), removeTop, REMOVE_WIDTH, REMOVE_HEIGHT);
      final Text remove = TerminalScreenText.of("settings.trusted.remove");
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(textRenderer, remove, REMOVE_WIDTH - 4),
          originX + removeX() + (REMOVE_WIDTH - textRenderer.getWidth(remove) + 1) / 2,
          removeTop + (REMOVE_HEIGHT - 8) / 2,
          VanillaPalette.opaque(VanillaPalette.BUTTON_TEXT),
          true);
    }
  }
}
