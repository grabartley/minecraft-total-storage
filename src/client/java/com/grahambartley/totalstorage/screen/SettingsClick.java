package com.grahambartley.totalstorage.screen;

public final class SettingsClick {
  private SettingsClick() {}

  public static SettingsAction of(
      int button, boolean overRow, boolean overRole, boolean mayAdminister) {
    if (!overRow || (button != GridClick.LEFT_BUTTON && button != GridClick.RIGHT_BUTTON)) {
      return SettingsAction.NONE;
    }
    if (!overRole) {
      return button == GridClick.LEFT_BUTTON ? SettingsAction.LOCATE : SettingsAction.NONE;
    }
    if (!mayAdminister) {
      return SettingsAction.REFUSED;
    }
    return button == GridClick.RIGHT_BUTTON
        ? SettingsAction.CYCLE_BACK
        : SettingsAction.CYCLE_FORWARD;
  }
}
