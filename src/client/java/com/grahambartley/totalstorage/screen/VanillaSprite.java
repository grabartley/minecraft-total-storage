package com.grahambartley.totalstorage.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public enum VanillaSprite {
  BUTTON("widget/button"),
  BUTTON_HIGHLIGHTED("widget/button_highlighted"),
  BUTTON_DISABLED("widget/button_disabled"),
  TEXT_FIELD("widget/text_field"),
  TEXT_FIELD_HIGHLIGHTED("widget/text_field_highlighted"),
  SLOT("container/slot"),
  SCROLLER("container/creative_inventory/scroller"),
  SCROLLER_DISABLED("container/creative_inventory/scroller_disabled"),
  SEARCH("icon/search");

  private final String path;
  private final Identifier id;

  VanillaSprite(String path) {
    this.path = path;
    this.id = Identifier.ofVanilla(path);
  }

  public String spritePath() {
    return path;
  }

  public Identifier id() {
    return id;
  }

  public void draw(DrawContext context, int x, int y, int width, int height) {
    context.drawGuiTexture(id, x, y, width, height);
  }

  public static VanillaSprite button(boolean active, boolean hovered) {
    if (!active) {
      return BUTTON_DISABLED;
    }
    return hovered ? BUTTON_HIGHLIGHTED : BUTTON;
  }

  public static VanillaSprite field(boolean focused) {
    return focused ? TEXT_FIELD_HIGHLIGHTED : TEXT_FIELD;
  }
}
