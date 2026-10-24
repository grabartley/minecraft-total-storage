package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;

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

  public static TerminalTab at(int windowX, int windowY) {
    final int index = TerminalScreenLayout.tabAt(windowX, windowY);
    return index == TerminalScreenLayout.NO_CELL ? null : values()[index];
  }
}
