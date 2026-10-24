package com.grahambartley.totalstorage.screen;

import java.util.ArrayList;
import java.util.List;

public final class ScreenButtons {
  private ScreenButtons() {}

  public static List<ScreenButton> of(boolean playerInventory, boolean sortable) {
    final List<ScreenButton> buttons = new ArrayList<>(ScreenButton.values().length);
    if (playerInventory) {
      buttons.add(ScreenButton.QUICK_STACK);
    }
    if (sortable) {
      buttons.add(ScreenButton.SORT);
    }
    return List.copyOf(buttons);
  }
}
