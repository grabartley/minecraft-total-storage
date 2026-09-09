package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TerminalSearchBox {
  private static final Identifier BOX = TerminalSprites.of("search/box");
  private static final Identifier BOX_FOCUSED = TerminalSprites.of("search/box_focused");

  private static final Text HINT = TerminalScreenText.of("search.hint");

  private final TextFieldWidget field;

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

  public void drawBox(DrawContext context, int originX, int originY) {
    context.drawGuiTexture(
        field.isFocused() ? BOX_FOCUSED : BOX,
        originX + TerminalScreenLayout.SEARCH_X,
        originY + TerminalScreenLayout.SEARCH_Y,
        TerminalScreenLayout.SEARCH_WIDTH,
        TerminalScreenLayout.SEARCH_HEIGHT);
  }
}
