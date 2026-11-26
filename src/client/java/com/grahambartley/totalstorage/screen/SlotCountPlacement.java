package com.grahambartley.totalstorage.screen;

public record SlotCountPlacement(float scale, int x, int y) {
  public static final int FULL_SIZE_WIDTH = 12;
  public static final float SMALL_SCALE = 0.5F;
  public static final int RIGHT_EDGE = 17;
  public static final int TOP = 9;

  public static SlotCountPlacement of(int itemX, int itemY, int labelWidth, int lineHeight) {
    final float scale = labelWidth <= FULL_SIZE_WIDTH ? 1.0F : SMALL_SCALE;
    return new SlotCountPlacement(
        scale,
        Math.round((itemX + RIGHT_EDGE) / scale) - labelWidth,
        Math.round((itemY + TOP + lineHeight) / scale) - lineHeight);
  }
}
