package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public enum TerminalSprite {
  PANEL("chrome/panel"),
  SUNKEN("chrome/sunken"),
  RAISED("chrome/raised"),
  RAISED_LIT("chrome/raised_lit"),
  CHECK_LIGHT("icon/check_light"),
  CROSS("icon/cross"),
  CROSS_UNAVAILABLE("icon/cross_disabled"),
  FUZZY("icon/fuzzy"),
  FUZZY_OFF("icon/fuzzy_off"),
  RENAME("icon/rename"),
  DELETE("icon/delete"),
  ITEMS("icon/items"),
  SETTINGS("icon/settings"),
  QUICK_STACK("quick_stack_button/normal"),
  QUICK_STACK_HOVERED("quick_stack_button/hover"),
  QUICK_STACK_PRESSED("quick_stack_button/pressed"),
  QUICK_STACK_UNAVAILABLE("quick_stack_button/disabled"),
  SORT("sort_button/normal"),
  SORT_HOVERED("sort_button/hover"),
  SORT_PRESSED("sort_button/pressed"),
  SORT_UNAVAILABLE("sort_button/disabled"),
  LOCKED_SLOT("slot/locked"),
  EMPTY_NETWORK("empty/network"),
  NO_RESULTS("empty/no_results");

  private final String path;
  private final Identifier id;

  TerminalSprite(String path) {
    this.path = path;
    this.id = Identifier.of(TotalStorage.MOD_ID, path);
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
