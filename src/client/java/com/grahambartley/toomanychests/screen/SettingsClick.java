package com.grahambartley.toomanychests.screen;

/**
 * Turns a click on a settings row into what the player meant by it.
 *
 * <p>Split out for the reason {@link SettingsControl} gives about its own hit test: deciding this
 * inside a mouse handler puts a real rule somewhere it cannot be proven without a running client.
 *
 * <p>Left and right walk a value in opposite directions, matching {@link ContainerRoleCycle} and
 * {@link AccessModeCycle}. Every other button means nothing here and is handed back so the screen
 * behind can have it.
 *
 * <p>A click a player may not act on is still a click on the panel. Reporting it as {@link
 * SettingsAction#REFUSED} rather than as nothing is what stops it falling through to whatever sits
 * underneath, which for a row of this panel is the player's own inventory.
 */
public final class SettingsClick {
  private SettingsClick() {}

  /**
   * @param overSomething whether the click landed on a row or a control this panel owns
   * @param mayAdminister whether the server has said this player may change the terminal
   */
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
