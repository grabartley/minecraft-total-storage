package com.grahambartley.totalstorage.screen;

public enum TerminalTab {
  ITEMS,

  SETTINGS;

  public int index() {
    return ordinal();
  }

  public boolean isSettings() {
    return this == SETTINGS;
  }

  public String label() {
    return this == SETTINGS ? "settings" : "items";
  }

  public TerminalSprite icon() {
    return this == SETTINGS ? TerminalSprite.SETTINGS : TerminalSprite.ITEMS;
  }
}
