package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The terminal's search field: the chrome around it, and the text a player has typed into it.
 *
 * <p>The field draws no background of its own, so the box behind it can be the mod's own
 * nine-sliced sprite rather than vanilla's black rectangle. That leaves the widget covering only
 * the text area, which is what takes a click, and the border it sits inside is drawn around it.
 *
 * <p>The text is not held here. The widget is the only copy of it, so the box a player is looking
 * at and the query a request carries cannot drift apart.
 *
 * <p>The hint is a suggestion rather than a placeholder, because vanilla draws a placeholder only
 * while the field has no focus and this one takes focus the moment the terminal opens. A player
 * would never see it, and an unlabelled box is just a rectangle.
 */
public final class TerminalSearchBox {
  private static final Identifier BOX = TerminalSprites.of("search/box");
  private static final Identifier BOX_FOCUSED = TerminalSprites.of("search/box_focused");

  private static final Text HINT = TerminalScreenText.of("search.hint");

  private final TextFieldWidget field;

  /**
   * @param originX where the terminal window sits on screen
   * @param originY where the terminal window sits on screen
   * @param previous what was already typed, which a resize must not throw away
   */
  public TerminalSearchBox(TextRenderer textRenderer, int originX, int originY, String previous) {
    field =
        new TextFieldWidget(
            textRenderer,
            originX + TerminalScreenLayout.SEARCH_TEXT_X,
            originY + TerminalScreenLayout.SEARCH_TEXT_Y,
            TerminalScreenLayout.SEARCH_TEXT_WIDTH,
            TerminalScreenLayout.SEARCH_TEXT_HEIGHT,
            TerminalScreenText.of("search"));
    field.setDrawsBackground(false);
    field.setMaxLength(ListingRequestPayload.MAX_SEARCH_LENGTH);
    field.setChangedListener(this::showHintWhileEmpty);
    field.setText(previous);
  }

  private void showHintWhileEmpty(String text) {
    field.setSuggestion(text.isEmpty() ? HINT.getString() : null);
  }

  public TextFieldWidget widget() {
    return field;
  }

  public String text() {
    return field.getText();
  }

  public boolean isSearching() {
    return !field.getText().isEmpty();
  }

  public boolean isFocused() {
    return field.isFocused();
  }

  /**
   * Takes the field off screen with its tab. Not painting the box behind it would leave the text,
   * the caret and the suggestion drawn over whatever the other view puts there, and the field would
   * still be holding the keyboard.
   */
  public void setVisible(boolean visible) {
    field.visible = visible;
    if (!visible) {
      field.setFocused(false);
    }
  }

  public void clear() {
    field.setText("");
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    return field.keyPressed(keyCode, scanCode, modifiers);
  }

  /** The box the field is written into, which shows whether the terminal is listening. */
  public void drawBox(DrawContext context, int originX, int originY) {
    context.drawGuiTexture(
        field.isFocused() ? BOX_FOCUSED : BOX,
        originX + TerminalScreenLayout.SEARCH_X,
        originY + TerminalScreenLayout.SEARCH_Y,
        TerminalScreenLayout.SEARCH_WIDTH,
        TerminalScreenLayout.SEARCH_HEIGHT);
  }
}
