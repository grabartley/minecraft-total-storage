package com.grahambartley.toomanychests.render;

import com.grahambartley.toomanychests.storage.ContainerRole;

/**
 * What colour a container's outline is drawn in, by the role its network gave it.
 *
 * <p>Colour alone would leave the one distinction that matters unreadable to a player who cannot
 * separate red from green, so the outline of an excluded container is drawn twice, inset by a
 * block's edge, and reads as a double line rather than a differently coloured one. See the
 * accessibility note in {@code docs/terminal-gui-sprites.md}: every state here differs by shape as
 * well as by hue.
 */
public final class HighlightColour {
  private static final int GENERAL = 0xFFE4E9F2;
  private static final int FILTERED = 0xFF4FD08A;
  private static final int EXCLUDED = 0xFFC43A2E;

  private HighlightColour() {}

  public static int of(ContainerRole role) {
    return switch (role == null ? ContainerRole.DEFAULT : role) {
      case GENERAL -> GENERAL;
      case FILTERED -> FILTERED;
      case EXCLUDED -> EXCLUDED;
    };
  }

  /** Whether this role's outline is doubled, which is what tells it apart without colour. */
  public static boolean isDoubled(ContainerRole role) {
    return role == ContainerRole.EXCLUDED;
  }

  public static float red(int argb) {
    return (argb >> 16 & 0xFF) / 255.0f;
  }

  public static float green(int argb) {
    return (argb >> 8 & 0xFF) / 255.0f;
  }

  public static float blue(int argb) {
    return (argb & 0xFF) / 255.0f;
  }

  public static float alpha(int argb) {
    return (argb >>> 24) / 255.0f;
  }
}
