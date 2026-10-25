package com.grahambartley.totalstorage.screen;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class WithdrawAmountPrompt {
  private static final int GAP = 5;
  private static final int KEYS_GAP = 6;

  private ItemVariant variant;
  private AmountEntry entry;

  public void open(ItemVariant asked, int ceiling) {
    variant = asked;
    entry = new AmountEntry(ceiling);
  }

  public void close() {
    variant = null;
    entry = null;
  }

  public boolean isOpen() {
    return variant != null;
  }

  public ItemVariant variant() {
    return variant;
  }

  public int amount() {
    return entry == null ? 0 : entry.value();
  }

  public int requested() {
    return entry == null ? 0 : entry.requested();
  }

  public boolean asksForMoreThanThereIs() {
    return entry != null && entry.asksForMoreThanThereIs();
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    return isOpen() ? entry.keyPressed(keyCode, selectAllHeld) : PromptKey.HANDLED;
  }

  public PromptKey charTyped(char typed) {
    return isOpen() ? entry.charTyped(typed) : PromptKey.HANDLED;
  }

  public static final PromptFrame FRAME =
      PromptFrame.centred(
          PromptHeader.HEIGHT + GAP + PromptField.HEIGHT + KEYS_GAP + KeyCaps.HEIGHT);

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    final int left = originX + FRAME.contentLeft();
    int top = originY + FRAME.contentTop();
    PromptFrame.begin(context);
    FRAME.drawPanel(context, originX, originY);
    PromptHeader.draw(
        context,
        textRenderer,
        variant.toStack(),
        TerminalScreenText.of("amount.title", variant.toStack().getName()),
        left,
        top,
        PromptFrame.CONTENT_WIDTH);
    top += PromptHeader.HEIGHT + GAP;
    PromptField.draw(
        context,
        textRenderer,
        left,
        top,
        PromptFrame.CONTENT_WIDTH,
        entry.text(),
        TerminalScreenText.of("amount.placeholder"),
        TerminalScreenText.of("amount.of", ItemCountFormat.exact(entry.ceiling())),
        false);
    top += PromptField.HEIGHT + KEYS_GAP;
    KeyCaps.draw(
        context,
        textRenderer,
        HintLine.of(
            HintLine.key("enter"),
            HintLine.words(entry.takesEverything() ? "hint.take_all" : "hint.take"),
            HintLine.key("esc"),
            HintLine.words("hint.cancel")),
        left,
        top,
        VanillaPalette.MUTED,
        false);
    PromptFrame.end(context);
  }

  public static boolean isInside(int windowX, int windowY) {
    return FRAME.isInside(windowX, windowY);
  }
}
