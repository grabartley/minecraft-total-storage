package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.ListingRequestPayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class TerminalSearchBox {
  private static final Text HINT = TerminalScreenText.of("search.hint");

  private final TextRenderer textRenderer;

  private final TextFieldWidget field;

  public TerminalSearchBox(TextRenderer textRenderer, int originX, int originY, String previous) {
    this.textRenderer = textRenderer;
    field =
        new TextFieldWidget(
            textRenderer,
            originX + TerminalScreenLayout.SEARCH_TEXT_X,
            originY + TerminalScreenLayout.SEARCH_TEXT_Y,
            TerminalScreenLayout.SEARCH_TEXT_WIDTH,
            TerminalScreenLayout.SEARCH_TEXT_HEIGHT,
            TerminalScreenText.of("search"));
    field.setDrawsBackground(false);
    field.setEditableColor(VanillaPalette.FIELD_TEXT);
    field.setMaxLength(ListingRequestPayload.MAX_SEARCH_LENGTH);
    field.setText(previous);
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
    (field.isFocused() ? TerminalSprite.FIELD_FOCUSED : TerminalSprite.FIELD)
        .draw(
            context,
            originX + TerminalScreenLayout.SEARCH_X,
            originY + TerminalScreenLayout.SEARCH_Y,
            TerminalScreenLayout.SEARCH_WIDTH,
            TerminalScreenLayout.SEARCH_HEIGHT);
    if (isSearching()) {
      return;
    }
    context.drawText(
        textRenderer,
        HINT,
        originX + TerminalScreenLayout.SEARCH_TEXT_X,
        originY + TerminalScreenLayout.SEARCH_TEXT_Y,
        VanillaPalette.PANEL,
        true);
  }
}
