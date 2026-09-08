package com.grahambartley.toomanychests.screen;

/**
 * What a click on the settings panel means.
 *
 * <p>The same reason {@link GridClick} and {@link SettingsControl} exist: a rule about which button
 * does what, written inline in a mouse handler, is a rule nobody can check against the design. Here
 * it also carries who is allowed to do it, because a click that changes nothing has to be told
 * apart from one that was never over anything.
 */
public enum SettingsAction {
  /** Walk the role forwards, which is what a left click does. */
  CYCLE_FORWARD,
  /** Walk it back, so a role overshot by one click costs one click. */
  CYCLE_BACK,
  /** The click landed on something, but this player may not change it. */
  REFUSED,
  /** It landed on nothing this panel owns. */
  NONE
}
