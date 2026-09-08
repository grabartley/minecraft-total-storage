package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.ArrayList;
import java.util.List;
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
 * <p>The panel wears the recess sprite rather than the chassis, because its words are drawn in the
 * palette's glyph colours and those are meant to be read against the dark screen. On the chassis
 * they are pale grey on pale grey.
 *
 * <p>Every line is wrapped and the panel is sized around the result rather than the other way
 * round. English happens to fit a fixed panel; a translation does not, and an unwrapped line runs
 * out past both edges of the panel it is supposed to be inside.
 *
 * <p>The number itself lives in {@link AmountEntry}, which is where the rule about what may be
 * typed is decided and tested. This class draws it and nothing else.
 */
public final class WithdrawAmountPrompt {
  private static final Identifier PANEL = TerminalSprites.of("panel/inset");
  private static final Identifier FIELD = TerminalSprites.of("search/box_focused");

  private static final int PADDING = 8;
  private static final int LINE_HEIGHT = 9;
  private static final int FIELD_HEIGHT = 12;
  private static final int GAP = 4;

  /** Narrow enough to leave the grid readable around it, wide enough not to wrap "Up to 64". */
  private static final int MIN_WIDTH = 120;

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
    return isOpen() ? entry.keyPressed(keyCode) : PromptKey.HANDLED;
  }

  public PromptKey charTyped(char typed) {
    return isOpen() ? entry.charTyped(typed) : PromptKey.HANDLED;
  }

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    final int width = Math.min(TerminalScreenLayout.GRID_WIDTH, MIN_WIDTH);
    final int inner = width - 2 * PADDING;
    final List<OrderedText> heading =
        textRenderer.wrapLines(
            TerminalScreenText.of("amount.title", variant.toStack().getName()), inner);
    final List<OrderedText> available =
        textRenderer.wrapLines(TerminalScreenText.of("amount.available", entry.ceiling()), inner);
    final List<OrderedText> hint =
        textRenderer.wrapLines(TerminalScreenText.of("amount.hint"), inner);
    final int height =
        2 * PADDING
            + 3 * GAP
            + FIELD_HEIGHT
            + LINE_HEIGHT * (heading.size() + available.size() + hint.size());

    final int left =
        originX + TerminalScreenLayout.GRID_X + (TerminalScreenLayout.GRID_WIDTH - width) / 2;
    final int top =
        originY + TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - height) / 2;

    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    context.drawGuiTexture(PANEL, left, top, width, height);
    int lineY = top + PADDING;
    lineY = drawLines(context, textRenderer, heading, left, lineY, width, LABEL_COLOUR) + GAP;
    lineY = drawLines(context, textRenderer, available, left, lineY, width, HINT_COLOUR) + GAP;
    context.drawGuiTexture(FIELD, left + PADDING, lineY, inner, FIELD_HEIGHT);
    drawLines(
        context,
        textRenderer,
        List.of(Text.literal(entry.text()).asOrderedText()),
        left,
        lineY + 2,
        width,
        LABEL_COLOUR);
    drawLines(context, textRenderer, hint, left, lineY + FIELD_HEIGHT + GAP, width, HINT_COLOUR);
    context.getMatrices().pop();
  }

  /** Draws a wrapped block centred in the panel, and answers where the next one starts. */
  private static int drawLines(
      DrawContext context,
      TextRenderer textRenderer,
      List<OrderedText> lines,
      int left,
      int top,
      int width,
      int colour) {
    int lineY = top;
    for (final OrderedText line : new ArrayList<>(lines)) {
      context.drawText(
          textRenderer,
          line,
          left + (width - textRenderer.getWidth(line)) / 2,
          lineY,
          colour,
          false);
      lineY += LINE_HEIGHT;
    }
    return lineY;
  }
}
