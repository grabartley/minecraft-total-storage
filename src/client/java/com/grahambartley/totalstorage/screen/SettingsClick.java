package com.grahambartley.totalstorage.screen;

public final class SettingsClick {
  private SettingsClick() {}

  public static SettingsAction of(
      int button,
      ContainerRowTarget target,
      boolean shiftDown,
      boolean mayAdminister,
      boolean filtered) {
    if (target == ContainerRowTarget.NONE
        || (button != GridClick.LEFT_BUTTON && button != GridClick.RIGHT_BUTTON)) {
      return SettingsAction.NONE;
    }
    if (shiftDown) {
      return editor(button, mayAdminister, filtered);
    }
    if (target == ContainerRowTarget.BODY) {
      return button == GridClick.LEFT_BUTTON ? SettingsAction.LOCATE : SettingsAction.NONE;
    }
    if (!mayAdminister) {
      return SettingsAction.REFUSED;
    }
    return button == GridClick.RIGHT_BUTTON
        ? SettingsAction.CYCLE_BACK
        : SettingsAction.CYCLE_FORWARD;
  }

  private static SettingsAction editor(int button, boolean mayAdminister, boolean filtered) {
    if (!mayAdminister) {
      return SettingsAction.REFUSED;
    }
    if (button == GridClick.RIGHT_BUTTON) {
      return SettingsAction.RENAME;
    }
    return filtered ? SettingsAction.CHOOSE_TAGS : SettingsAction.REFUSED;
  }
}
