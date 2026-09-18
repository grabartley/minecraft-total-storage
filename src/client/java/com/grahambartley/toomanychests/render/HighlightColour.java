package com.grahambartley.toomanychests.render;

import com.grahambartley.toomanychests.storage.ContainerRole;

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

  public static HighlightShape shapeOf(ContainerRole role) {
    return switch (role == null ? ContainerRole.DEFAULT : role) {
      case GENERAL -> HighlightShape.SOLID;
      case FILTERED -> HighlightShape.DASHED;
      case EXCLUDED -> HighlightShape.DOUBLED;
    };
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
