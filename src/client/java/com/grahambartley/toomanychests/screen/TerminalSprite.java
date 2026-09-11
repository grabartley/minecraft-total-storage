package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public enum TerminalSprite {
  PANEL("chrome/panel"),
  SUNKEN("chrome/sunken"),
  RAISED("chrome/raised"),
  RAISED_LIT("chrome/raised_lit"),
  PRESSED("chrome/pressed"),
  FLAT("chrome/flat"),
  FIELD("chrome/field"),
  FIELD_FOCUSED("chrome/field_focused"),
  CHECK("icon/check"),
  CLEAR_ALL("icon/clear_all"),
  DEPOSIT_ALL("icon/deposit_all"),
  HIGHLIGHT("icon/highlight"),
  ITEMS("icon/items"),
  RESCAN("icon/rescan"),
  SETTINGS("icon/settings"),
  EMPTY_NETWORK("empty/network"),
  NO_RESULTS("empty/no_results");

  private final String path;
  private final Identifier id;

  TerminalSprite(String path) {
    this.path = path;
    this.id = Identifier.of(TooManyChests.MOD_ID, path);
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
}
