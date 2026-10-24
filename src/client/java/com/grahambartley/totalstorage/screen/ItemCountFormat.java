package com.grahambartley.totalstorage.screen;

import java.util.Locale;

public final class ItemCountFormat {
  public static final long COMPACT_ABOVE = 9_999L;

  private static final String[] UNITS = {"k", "M", "B", "T"};
  private static final long STEP = 1_000L;

  private static final long DECIMAL_BELOW = 100L;

  private ItemCountFormat() {}

  public static String cell(long count) {
    if (count <= COMPACT_ABOVE) {
      return exact(count);
    }
    long scaled = count;
    int unit = -1;
    while (scaled >= STEP && unit < UNITS.length - 1) {
      scaled /= STEP;
      unit++;
    }
    final long remainder = count / divisorFor(unit) % STEP;
    if (scaled >= DECIMAL_BELOW) {
      return scaled + UNITS[unit];
    }
    return scaled + "." + remainder / 100L + UNITS[unit];
  }

  public static String exact(long count) {
    return String.format(Locale.ROOT, "%,d", count);
  }

  private static long divisorFor(int unit) {
    long divisor = 1L;
    for (int step = 0; step < unit; step++) {
      divisor *= STEP;
    }
    return divisor;
  }
}
