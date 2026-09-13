package com.grahambartley.toomanychests.config;

public record ClientPreferences(
    boolean sortButtonVisible,
    ScreenButtonAnchor screenButtonAnchor,
    boolean lockedSlotTintVisible) {
  public static final boolean DEFAULT_SORT_BUTTON_VISIBLE = true;
  public static final ScreenButtonAnchor DEFAULT_SCREEN_BUTTON_ANCHOR =
      ScreenButtonAnchor.INSIDE_TOP_RIGHT;
  public static final boolean DEFAULT_LOCKED_SLOT_TINT_VISIBLE = true;

  private static final ClientPreferences DEFAULTS =
      new ClientPreferences(
          DEFAULT_SORT_BUTTON_VISIBLE,
          DEFAULT_SCREEN_BUTTON_ANCHOR,
          DEFAULT_LOCKED_SLOT_TINT_VISIBLE);

  public ClientPreferences {
    screenButtonAnchor =
        screenButtonAnchor == null ? DEFAULT_SCREEN_BUTTON_ANCHOR : screenButtonAnchor;
  }

  public static ClientPreferences defaults() {
    return DEFAULTS;
  }

  public ClientPreferences withSortButtonVisible(boolean visible) {
    return new ClientPreferences(visible, screenButtonAnchor, lockedSlotTintVisible);
  }

  public ClientPreferences withScreenButtonAnchor(ScreenButtonAnchor anchor) {
    return new ClientPreferences(sortButtonVisible, anchor, lockedSlotTintVisible);
  }

  public ClientPreferences withLockedSlotTintVisible(boolean visible) {
    return new ClientPreferences(sortButtonVisible, screenButtonAnchor, visible);
  }
}
