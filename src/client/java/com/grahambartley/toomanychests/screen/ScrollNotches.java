package com.grahambartley.toomanychests.screen;

public final class ScrollNotches {
  public static final int ROWS_PER_NOTCH = 3;

  public static final int GRID_ROWS_PER_NOTCH = 1;

  public static int gridRowsFor(double verticalAmount) {
    return rowsFor(verticalAmount, GRID_ROWS_PER_NOTCH);
  }

  private ScrollNotches() {}

  public static int rowsFor(double verticalAmount) {
    return rowsFor(verticalAmount, ROWS_PER_NOTCH);
  }

  public static int rowsFor(double verticalAmount, int rowsPerNotch) {
    if (verticalAmount == 0.0 || rowsPerNotch < 1) {
      return 0;
    }
    final int notches = Math.max(1, (int) Math.round(Math.abs(verticalAmount)));
    final int rows = notches * rowsPerNotch;
    return verticalAmount > 0.0 ? -rows : rows;
  }
}
