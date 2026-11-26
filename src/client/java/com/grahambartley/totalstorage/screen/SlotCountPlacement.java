package com.grahambartley.totalstorage.screen;

public record SlotCountPlacement(float scale, int x, int y) {
  public static final int FIT_WIDTH = 16;
  public static final int RIGHT_EDGE = 17;
  public static final int TOP = 9;

  public static SlotCountPlacement of(int itemX, int itemY, int labelWidth, int lineHeight) {
    final float scale = Math.min(1.0F, (float) FIT_WIDTH / labelWidth);
    return new SlotCountPlacement(
        scale,
        Math.round((itemX + RIGHT_EDGE) / scale) - labelWidth,
        Math.round((itemY + TOP + lineHeight) / scale) - lineHeight);
  }
}
