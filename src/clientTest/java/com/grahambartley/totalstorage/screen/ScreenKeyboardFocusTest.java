package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScreenKeyboardFocusTest {
  @Test
  void nothingFocusedIsNobodyTyping() {
    assertFalse(ScreenKeyboardFocus.isTyping(null));
  }

  @Test
  void aFocusedTextFieldOwnsTheKeyboard() {
    assertTrue(ScreenKeyboardFocus.isTyping(field(true)));
  }

  @Test
  void aTextFieldThatIsSwitchedOffOwnsNothing() {
    assertFalse(ScreenKeyboardFocus.isTyping(field(false)));
  }

  @Test
  void aWidgetThatIsNotATextFieldOwnsNothing() {
    assertFalse(ScreenKeyboardFocus.isTyping(Mockito.mock(net.minecraft.client.gui.Element.class)));
  }

  private static TextFieldWidget field(boolean active) {
    final TextFieldWidget widget =
        new TextFieldWidget(Mockito.mock(TextRenderer.class), 0, 0, 10, 10, Text.empty());
    widget.active = active;
    widget.visible = active;
    widget.setFocused(active);
    return widget;
  }
}
