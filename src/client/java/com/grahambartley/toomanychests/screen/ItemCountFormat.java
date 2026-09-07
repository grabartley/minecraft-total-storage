package com.grahambartley.toomanychests.screen;

import java.util.Locale;

/**
 * How many of something the network holds, written to fit where it is being written.
 *
 * <p>A grid cell is twenty pixels wide and a network reaches numbers a chest never does, so the
 * count drawn on a cell is compacted once it stops fitting. The tooltip is not compacted, because
 * that is where a player goes when the compact form is not the answer they wanted.
 *
 * <p>Compaction truncates rather than rounds. A terminal saying {@code 12.3k} when it holds 12,399
 * is understating what is there; saying {@code 12.4k} would be promising items that are not, and a
 * storage display that overstates is the one that gets a player caught short.
 */
public final class ItemCountFormat {
  /** Where compaction starts, which is the first count that will not fit a cell in full. */
  public static final long COMPACT_ABOVE = 9_999L;

  private static final String[] UNITS = {"k", "M", "B", "T"};
  private static final long STEP = 1_000L;

  /** Below this the compacted number keeps a decimal place, above it there is no room for one. */
  private static final long DECIMAL_BELOW = 100L;

  private ItemCountFormat() {}

  /** The count as drawn on a grid cell, compacted once the exact form stops fitting. */
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

  /** The count in full, grouped so a seven digit total is still readable in a tooltip. */
  public static String exact(long count) {
    return String.format(Locale.ROOT, "%,d", count);
  }

  /**
   * What the count was divided by to reach the given unit, which is what the digits dropped on the
   * way have to be recovered from to produce the decimal place.
   */
  private static long divisorFor(int unit) {
    long divisor = 1L;
    for (int step = 0; step < unit; step++) {
      divisor *= STEP;
    }
    return divisor;
  }
}
