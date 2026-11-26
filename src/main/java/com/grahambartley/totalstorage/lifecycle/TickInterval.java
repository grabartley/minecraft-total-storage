package com.grahambartley.totalstorage.lifecycle;

public record TickInterval(int ticks) {
  public TickInterval {
    if (ticks <= 0) {
      throw new IllegalArgumentException("an interval must be at least one tick: " + ticks);
    }
  }

  public boolean isDue(int tick) {
    return Math.floorMod(tick, ticks) == 0;
  }
}
