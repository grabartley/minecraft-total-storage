package com.grahambartley.toomanychests.screen;

public final class SettingsClick {
  private SettingsClick() {}

  public static SettingsAction of(int button, boolean overSomething, boolean mayAdminister) {
    if (!overSomething || (button != GridClick.LEFT_BUTTON && button != GridClick.RIGHT_BUTTON)) {
      return SettingsAction.NONE;
    }
    if (!mayAdminister) {
      return SettingsAction.REFUSED;
    }
    return button == GridClick.RIGHT_BUTTON
        ? SettingsAction.CYCLE_BACK
        : SettingsAction.CYCLE_FORWARD;
  }
}
