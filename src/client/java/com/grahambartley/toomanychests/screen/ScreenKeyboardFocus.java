package com.grahambartley.toomanychests.screen;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;

public final class ScreenKeyboardFocus {
  private ScreenKeyboardFocus() {}

  public static boolean isTyping(Element focused) {
    if (focused instanceof TextFieldWidget field) {
      return field.isActive();
    }
    return focused instanceof ParentElement parent && isTyping(parent.getFocused());
  }
}
