package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Asks how much of one item to take, for the amounts no click can express.
 *
 * <p>A panel drawn over the grid rather than a widget added to the screen, because Minecraft
 * rebuilds every widget on a resize and a prompt that vanished when a player changed their GUI
 * scale would lose what they had typed. Nothing here is focusable, so the search box keeps its
 * focus and gets it back untouched when the prompt closes.
 *
 * <p>The number itself lives in {@link AmountEntry}, which is where the rule about what may be
 * typed is decided and tested. This class draws it and nothing else.
 */
public final class WithdrawAmountPrompt {
  private static final Identifier PANEL = Identifier.of(TooManyChests.MOD_ID, "panel/background");
  private static final Identifier FIELD = Identifier.of(TooManyChests.MOD_ID, "search/box_focused");

  private static final int WIDTH = 132;
  private static final int PADDING = 8;
  private static final int LINE_HEIGHT = 9;
  private static final int FIELD_HEIGHT = 12;
  private static final int GAP = 4;
  private static final int HEIGHT = 2 * PADDING + 3 * LINE_HEIGHT + FIELD_HEIGHT + 3 * GAP;

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int HINT_COLOUR = 0x9AA3B3;

  /** Far enough forward to sit over the cells and the items drawn in them. */
  private static final float ELEVATION = 300.0F;

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

  public PromptKey keyPressed(int keyCode) {
    return isOpen() ? entry.keyPressed(keyCode) : PromptKey.IGNORED;
  }

  public PromptKey charTyped(char typed) {
    return isOpen() ? entry.charTyped(typed) : PromptKey.IGNORED;
  }

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    final int left =
        originX + TerminalScreenLayout.GRID_X + (TerminalScreenLayout.GRID_WIDTH - WIDTH) / 2;
    final int top =
        originY + TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - HEIGHT) / 2;
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    context.drawGuiTexture(PANEL, left, top, WIDTH, HEIGHT);
    drawCentred(
        context,
        textRenderer,
        TerminalScreenText.of("amount.title", variant.toStack().getName()).asOrderedText(),
        left,
        top + PADDING,
        LABEL_COLOUR);
    drawCentred(
        context,
        textRenderer,
        TerminalScreenText.of("amount.available", entry.ceiling()).asOrderedText(),
        left,
        top + PADDING + LINE_HEIGHT + GAP,
        HINT_COLOUR);
    final int fieldTop = top + PADDING + 2 * LINE_HEIGHT + 2 * GAP;
    context.drawGuiTexture(FIELD, left + PADDING, fieldTop, WIDTH - 2 * PADDING, FIELD_HEIGHT);
    drawCentred(
        context,
        textRenderer,
        Text.literal(entry.text()).asOrderedText(),
        left,
        fieldTop + 2,
        LABEL_COLOUR);
    drawCentred(
        context,
        textRenderer,
        TerminalScreenText.of("amount.hint").asOrderedText(),
        left,
        fieldTop + FIELD_HEIGHT + GAP,
        HINT_COLOUR);
    context.getMatrices().pop();
  }

  private static void drawCentred(
      DrawContext context,
      TextRenderer textRenderer,
      OrderedText line,
      int left,
      int y,
      int colour) {
    context.drawText(
        textRenderer, line, left + (WIDTH - textRenderer.getWidth(line)) / 2, y, colour, false);
  }
}
